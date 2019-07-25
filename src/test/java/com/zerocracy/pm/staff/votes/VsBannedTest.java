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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.staff.Bans;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsBanned}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsBannedTest {

    @Test
    public void highRankForBanned() throws IOException {
        final FkProject proj = new FkProject();
        final String login = "caarlos0";
        final String job = "gh:test/job#1";
        new Bans(proj).bootstrap().ban(job, login, "Issue reporter");
        MatcherAssert.assertThat(
            "Banned voter didn't give high rank for banned user",
            new VsBanned(
                proj,
                job
            ).take(login, new StringBuilder()),
            Matchers.equalTo(1.0)
        );
    }

    @Test
    public void lowRankIfNotBanned() throws IOException {
        MatcherAssert.assertThat(
            "Banned voter didn't give low rank for not banned user",
            new VsBanned(
                new FkProject(),
                "gh:test/job#2"
            ).take("yegor256", new StringBuilder()),
            Matchers.equalTo(0.0)
        );
    }
}
