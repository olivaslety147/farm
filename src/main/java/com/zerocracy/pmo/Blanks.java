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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.time.Instant;
import org.cactoos.iterable.ItemAt;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.NumberOf;
import org.cactoos.text.JoinedText;
import org.xembly.Directives;

/**
 * Blanks.
 * <p>
 * User metric which represents the amount of bugs user reported,
 * which were not counted as bugs by project architects.
 *
 * @since 1.0
 */
public final class Blanks {

    /**
     * Project.
     */
    private final Project pmo;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Ctor.
     * @param farm The farm
     * @param user The user
     */
    public Blanks(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pkt Pmo project
     * @param user The user
     */
    public Blanks(final Project pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * Add a blank.
     * @param proj Project
     * @param job Job id
     * @param kind Job kind
     * @throws IOException If fails
     */
    public void add(final Project proj, final String job, final String kind)
        throws IOException {
        this.add(proj, job, kind, Instant.now());
    }

    /**
     * Add a blank.
     * @param proj Project
     * @param job Job id
     * @param kind Job kind
     * @param time Added time
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final Project proj, final String job, final String kind,
        final Instant time) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/blanks")
                    .push()
                    .xpath(String.format("blank[@job = '%s']", job))
                    .remove()
                    .pop()
                    .add("blank")
                    .attr("job", job)
                    .add("project")
                    .set(proj.pid())
                    .up()
                    .add("kind")
                    .set(kind)
                    .up()
                    .add("added")
                    .set(time)
            );
        }
    }

    /**
     * Iterate over all jobs in blanks.
     * @return Job ids iterable
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath("/blanks/blank/@job");
        }
    }

    /**
     * Total amount of blanks.
     * @return Blanks count
     * @throws IOException If fails
     */
    public int total() throws IOException {
        try (final Item item = this.item()) {
            return new NumberOf(
                new IoCheckedScalar<>(
                    new ItemAt<>(
                        new Xocument(item).xpath("count(/blanks/blank)")
                    )
                ).value()
            ).intValue();
        }
    }

    /**
     * Remove all blanks older than specified date.
     * @param date Date
     * @throws IOException If failed
     */
    public void removeOlderThan(final Instant date) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        new JoinedText(
                            "",
                            "/blanks/blank[xs:dateTime(added) < ",
                            "xs:dateTime('",
                            date.toString(),
                            "')]"
                        ).asString()
                    ).remove()
            );
        }
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Blanks bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/blanks");
        }
        return this;
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq(
            String.format("blanks/%s.xml", this.login)
        );
    }
}
