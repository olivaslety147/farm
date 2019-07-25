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

package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import java.io.IOException;
import javax.json.JsonObject;
import org.cactoos.text.FormattedText;

/**
 * Add issue to WBS if it labeled as bug.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnBug implements Rebound {
    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        new ClaimOut()
            .type("Add job to WBS")
            .token(new TokenOfIssue(issue))
            .param("job", new Job(issue))
            .param("reason", "GitHub label was attached")
            .param("quiet", true)
            .postTo(new ClaimsOf(farm, new GhProject(farm, issue.repo())));
        return new FormattedText(
            "Issue #%d added to WBS by 'bug' label",
            issue.number()
        ).asString();
    }
}
