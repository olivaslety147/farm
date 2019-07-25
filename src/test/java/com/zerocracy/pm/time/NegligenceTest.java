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
package com.zerocracy.pm.time;

import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Negligence;
import java.time.Duration;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Negligence}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class NegligenceTest {

    @Test
    public void bootstraps() throws Exception {
        MatcherAssert.assertThat(
            new Negligence(new FkFarm(new FkProject()), "amihaiemil")
                .bootstrap(),
            Matchers.allOf(
                Matchers.instanceOf(Negligence.class),
                Matchers.notNullValue()
            )
        );
    }

    @Test
    public void returnsNoDelays() throws Exception {
        MatcherAssert.assertThat(
            new Negligence(new FkFarm(new FkProject()), "g4s8")
                .bootstrap().delays(),
            Matchers.is(0)
        );
    }

    @Test
    public void registersDelay() throws Exception {
        final Negligence negligence = new Negligence(
            new FkFarm(new FkProject()), "krzyk"
        ).bootstrap();
        negligence.add(new FkProject(), "gh:test/test#1");
        MatcherAssert.assertThat(
            negligence.delays(),
            Matchers.is(1)
        );
    }

    @Test
    public void removeOldItems() throws Exception {
        final Project pkt = new FkProject();
        final Negligence negligence = new Negligence(new FkFarm(), "user4")
            .bootstrap();
        final String job = "gh:test/test#11";
        final Instant time = Instant.ofEpochMilli(1563956761000L);
        negligence.add(pkt, job, time);
        // @checkstyle MagicNumberCheck (1 line)
        negligence.removeOlderThan(time.plus(Duration.ofDays(91L)));
        MatcherAssert.assertThat(negligence.delays(), Matchers.is(0));
    }
}
