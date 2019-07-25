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
package com.zerocracy.pm.staff;

import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Roles}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class BansTest {

    @Test
    public void addsAndRemovesBans() throws Exception {
        final Bans bans = new Bans(new FkProject()).bootstrap();
        final String job = "gh:test/test#1";
        final String login = "davvd";
        bans.ban(job, login, "just for fun");
        MatcherAssert.assertThat(
            bans.reasons(job, login),
            Matchers.iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            bans.reasons(job),
            Matchers.iterableWithSize(1)
        );
    }

    @Test
    public void checkExistence() throws Exception {
        final Bans bans = new Bans(new FkProject()).bootstrap();
        final String job = "gh:test/test#2";
        final String login = "jimmy";
        bans.ban(job, login, "test");
        MatcherAssert.assertThat(
            bans.exists(job, login),
            Matchers.is(true)
        );
    }
}
