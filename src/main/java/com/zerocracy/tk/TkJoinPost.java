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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pm.staff.GlobalInviters;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Resumes;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Join Zerocracy form processing.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkJoinPost implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Constructor.
     * @param frm Farm
     */
    public TkJoinPost(final Farm frm) {
        this.farm = frm;
    }

    // @checkstyle ExecutableStatementCountCheck (100 lines)
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response act(final RqRegex req) throws IOException {
        final String author = new RqUser(this.farm, req, false).value();
        final People people = new People(this.farm).bootstrap();
        people.touch(author);
        if (people.hasMentor(author)) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "You already have a mentor (@%s), no need to rejoin."
                    ).say(people.mentor(author)),
                    Level.WARNING
                ),
                "/join"
            );
        }
        final LocalDateTime when;
        if (people.applied(author)) {
            when = LocalDateTime.ofInstant(
                people.appliedTime(author),
                ZoneOffset.UTC
            );
        } else {
            when = LocalDateTime.MIN;
        }
        final long days = (long) new Policy().get("1.lag", 16);
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (when.plusDays(days).isAfter(now)) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "You can apply only one time in %d days, ",
                        "you've applied %d days ago (%s)"
                    ).say(
                        days,
                        Duration.between(when, now).toDays(),
                        when.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    ),
                    Level.WARNING
                ),
                "/join"
            );
        }
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String telegram = form.single("telegram");
        final String personality = form.single("personality");
        final String about = form.single("about");
        final long stko = Long.parseLong(form.single("stackoverflow"));
        new Resumes(this.farm).bootstrap()
            .add(
                author,
                LocalDateTime.now(),
                about,
                personality,
                stko,
                telegram
            );
        new ClaimOut().type("Join form submitted")
            .author(author)
            .param("telegram", telegram)
            .param("stackoverflow", stko)
            .param("about", about)
            .param("personality", personality)
            .postTo(new ClaimsOf(this.farm));
        for (final String inv : new GlobalInviters(this.farm)) {
            new ClaimOut()
                .type("Notify user")
                .token(String.format("user;%s", inv))
                .param(
                    "message",
                    // @checkstyle LineLengthCheck (1 line)
                    new Par("Join form was submitted by @%s, you can check resumes page")
                        .say(author)
                )
                .param("min", new Policy().get("1.min-rep", 0))
                .param("reason", "New student")
                .postTo(new ClaimsOf(this.farm));
        }
        return new RsForward(
            new RsParFlash(
                new Par(
                    "The request will be sent to all high-ranked users"
                ).say(),
                Level.INFO
            )
        );
    }
}
