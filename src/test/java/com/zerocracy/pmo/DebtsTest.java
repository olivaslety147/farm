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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Par;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Test case for {@link Debts}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DebtsTest {

    @Test
    public void addsAndRemoves() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String uid = "yegor256";
        debts.add(uid, new Cash.S("$5"), "details 1", "reason 1");
        debts.add(uid, new Cash.S("$6"), "details 2", "reason 2");
        MatcherAssert.assertThat(
            debts.amount(uid),
            Matchers.equalTo(new Cash.S("$11"))
        );
    }

    @Test
    public void addsFailure() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String uid = "yegor256";
        debts.add(uid, new Cash.S("$99"), "details-11", "reason-22");
        MatcherAssert.assertThat(debts.expired(uid), Matchers.equalTo(true));
        debts.failure(uid, "Can't pay over PayPal");
        MatcherAssert.assertThat(debts.expired(uid), Matchers.equalTo(false));
    }

    @Test
    public void hasSameHash() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String login = "test124356";
        debts.add(
            login, Cash.ZERO,
            "Hello", "job:test/1",
            Instant.parse("2018-12-09T09:10:05.657Z")
        );
        MatcherAssert.assertThat(
            debts.hash(login),
            // @checkstyle LineLengthCheck (1 line)
            new IsEqual<>("5df6c2adfdf30347650285cfecd6be2e38df09ff6372b5598acdfa5ec4463be0")
        );
    }

    @Test
    public void printsSingleToXembly() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String uid = "0crat";
        debts.add(
            uid, new Cash.S("$99"),
            new Par("details-1 as in §1").say(),
            "reason-1"
        );
        debts.add(uid, new Cash.S("$17"), "details-15", "reason-15");
        debts.failure(uid, "Can't pay by PayPal");
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(debts.toXembly(uid)).xmlQuietly()
            ),
            XhtmlMatchers.hasXPaths(
                "/debt[@total='$116.00']",
                "/debt[count(item)=2]",
                "/debt/item[details='details-1 as in §1 (reason-1)']",
                "/debt[@failed]"
            )
        );
    }

    @Test
    public void oldest() throws Exception {
        final Debts debts = new Debts(new Pmo(new FkFarm())).bootstrap();
        final String uid = "0crat";
        debts.add(
            uid, new Cash.S("$2"),
            new Par("details-2 as in §1").say(),
            "reason-2",
            Instant.ofEpochMilli(2L)
        );
        debts.add(
            uid, new Cash.S("$1"),
            new Par("details-1 as in §1").say(),
            "reason-1",
            Instant.ofEpochMilli(1L)
        );
        debts.add(
            uid, new Cash.S("$3"),
            new Par("details-3 as in §1").say(),
            "reason-3",
            Instant.ofEpochMilli(0L)
        );
        MatcherAssert.assertThat(
            debts.olderThan(uid, Instant.ofEpochMilli(1L)),
            Matchers.is(true)
        );
    }

    @Test
    public void iterateWithFilter() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String uid = "g4s8";
        debts.add(
            uid, new Cash.S("$23.45"),
            "test filter details", "reason filter",
            Instant.EPOCH
        );
        final int failures = 14;
        for (int flr = 0; flr < failures; ++flr) {
            debts.failure(uid, String.format("failure-%d", flr));
        }
        MatcherAssert.assertThat(
            debts.iterate("failure/attempt >= 14"),
            Matchers.contains(uid)
        );
    }

    @Test
    public void iterateWithoutFailures() throws Exception {
        final Debts debts = new Debts(new FkFarm()).bootstrap();
        final String uid = "g4s9";
        debts.add(
            uid, new Cash.S("$23.46"),
            "test filter details2", "reason filter2",
            Instant.EPOCH
        );
        MatcherAssert.assertThat(
            debts.iterate("not(failure) or failure/attempt <= 10"),
            Matchers.contains(uid)
        );
    }
}
