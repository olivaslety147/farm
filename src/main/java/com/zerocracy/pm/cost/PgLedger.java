/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.pm.cost;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.jcabi.jdbc.Preparation;
import com.jcabi.jdbc.SingleOutcome;
import com.jcabi.xml.XML;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.xembly.Directives;

/**
 * Ledger in postgres.
 *
 * @since 1.0
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.StaticAccessToStaticFields")
public final class PgLedger {

    /**
     * Amount number format.
     */
    private static final DecimalFormat FMT_AMOUNT;

    static {
        final DecimalFormat fmt = new DecimalFormat();
        fmt.setGroupingUsed(false);
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(0);
        FMT_AMOUNT = fmt;
    }

    /**
     * Database.
     */
    private final DataSource data;

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Ctor.
     *
     * @param data Data
     * @param pkt Project
     */
    public PgLedger(final DataSource data, final Project pkt) {
        this.data = data;
        this.pkt = pkt;
    }

    /**
     * Add transactions.
     *
     * @param tns Transactions
     * @throws SQLException If database fails
     * @throws IOException If project fails
     */
    public void add(final Ledger.Transaction... tns) throws SQLException,
        IOException {
        final JdbcSession session = new JdbcSession(this.data)
            .autocommit(false);
        Optional<Long> parent = session
            .sql(
                String.join(
                    " ",
                    "SELECT id FROM ledger",
                    "WHERE project = ? ORDER BY id DESC LIMIT 1"
                )
            )
            .set(this.pkt.pid())
            .select(
                (rset, stmt) -> {
                    final Optional<Long> res;
                    if (rset.next()) {
                        res = Optional.of(rset.getLong(1));
                    } else {
                        res = Optional.empty();
                    }
                    return res;
                }
            );
        final Outcome<Long> outcome = new SingleOutcome<>(Long.class);
        for (final Ledger.Transaction txn : tns) {
            parent = Optional.of(
                session.sql(
                    String.join(
                        " ",
                        "INSERT INTO ledger",
                        "(id, project, parent, amount,",
                        "dt, dtx, ct, ctx, details)",
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        "RETURNING id"
                    )
                ).prepare(txn.preparation(this.pkt, parent)).insert(outcome)
            );
        }
        session.commit();
    }

    /**
     * Sum of fees for all projects for selected period.
     * @param time Time to start
     * @return Sum of fees
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public Cash fees(final Instant time) throws IOException {
        try {
            return new JdbcSession(this.data).sql(
                String.join(
                    " ",
                    "SELECT sum(amount) FROM ledger",
                    "WHERE dt = ? AND dtx = ?",
                    "AND created >= ?"
                )
            ).prepare(
                stmt -> {
                    stmt.setString(1, "liabilities");
                    stmt.setString(2, "zerocracy");
                    stmt.setTimestamp(3, Timestamp.from(time));
                }
            ).select(
                (Outcome<Cash>) (rset, stmt) -> {
                    if (!rset.next()) {
                        throw new SQLException("Empty result");
                    }
                    try {
                        return new Cash.S(
                            String.format(
                                "USD %s",
                                PgLedger.FMT_AMOUNT.format(
                                    rset.getBigDecimal(1)
                                )
                            )
                        );
                    } catch (final CashParsingException err) {
                        throw new SQLException(
                            "Failed to parse amount of cash", err
                        );
                    }
                }
            );
        } catch (final SQLException err) {
            throw new IOException("Failed to collect fees", err);
        }
    }

    /**
     * Bootstrap it.
     * <p>
     * Copy all existing transactions from XML to database and remove them in
     * xml.
     *
     * @param item XML item
     * @return Self
     * @throws IOException If XML fails
     * @throws SQLException If postgres fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public PgLedger bootstrap(final Item item) throws IOException,
        SQLException {
        final Xocument xml = new Xocument(item.path());
        final String xpath = "/ledger/transactions";
        if (!xml.nodes(xpath).isEmpty()) {
            final JdbcSession session =
                new JdbcSession(this.data).autocommit(false);
            // @checkstyle LineLength (2 line)
            for (final XML txn : xml.nodes("/ledger/transactions/transaction")) {
                session.sql("INSERT INTO ledger (id, project, parent, created, amount, dt, dtx, ct, ctx, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    .prepare(new PgLedger.XmlPreparation(txn, this.pkt))
                    .insert(Outcome.VOID);
            }
            xml.modify(
                new Directives()
                    .xpath(xpath)
                    .remove()
            );
            session.commit();
        }
        return this;
    }

    /**
     * Check if ledger table has any transacion from start time.
     * @param start Start time
     * @return True if doesn't have
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public boolean empty(final Instant start) throws IOException {
        try {
            return !new JdbcSession(this.data).sql(
                String.join(
                    "",
                    "SELECT exists(",
                    "SELECT 1 FROM ledger WHERE project = ? AND created >= ?",
                    ")"
                )
            ).prepare(
                stmt -> {
                    try {
                        stmt.setString(1, this.pkt.pid());
                    } catch (final IOException err) {
                        throw new SQLException(
                            "Can't read project id", err
                        );
                    }
                    stmt.setTimestamp(2, Timestamp.from(start));
                }
            ).select(new SingleOutcome<>(Boolean.class));
        } catch (final SQLException err) {
            throw new IOException(
                "Failed to find transactions for project", err
            );
        }
    }

    /**
     * Preparation for bootstrap.
     */
    private static final class XmlPreparation implements Preparation {

        /**
         * XML transaction.
         */
        private final XML txn;

        /**
         * Project.
         */
        private final Project pkt;

        /**
         * Ctor.
         *
         * @param node XML transaction node
         * @param pkt Project
         */
        private XmlPreparation(final XML node, final Project pkt) {
            this.txn = node;
            this.pkt = pkt;
        }

        @Override
        public void prepare(final PreparedStatement stmt) throws SQLException {
            stmt.setLong(
                1, Long.parseLong(this.txn.xpath("@id").get(0))
            );
            try {
                stmt.setString(2, this.pkt.pid());
            } catch (final IOException err) {
                throw new SQLException("Failed to read project id", err);
            }
            final List<String> xparent = this.txn.xpath("@parent");
            if (xparent.isEmpty()) {
                stmt.setNull(3, Types.BIGINT);
            } else {
                stmt.setLong(3, Long.parseLong(xparent.get(0)));
            }
            stmt.setTimestamp(
                4,
                Timestamp.from(
                    Instant.parse(this.txn.xpath("created/text()").get(0))
                )
            );
            try {
                stmt.setBigDecimal(
                    5,
                    new Cash.S(
                        this.txn.xpath("amount/text()").get(0)
                    ).decimal()
                );
            } catch (final CashParsingException err) {
                throw new SQLException("Failed to read amount cash", err);
            }
            stmt.setString(6, this.txn.xpath("dt/text()").get(0));
            stmt.setString(7, this.txn.xpath("dtx/text()").get(0));
            stmt.setString(8, this.txn.xpath("ct/text()").get(0));
            stmt.setString(9, this.txn.xpath("ctx/text()").get(0));
            stmt.setString(
                10, this.txn.xpath("details/text()").get(0)
            );
        }
    }
}
