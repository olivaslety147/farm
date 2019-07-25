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
package com.zerocracy.farm;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.entry.ExtDynamo;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Errors}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ErrorsITCase {
    @Test
    public void fetchItems() throws Exception {
        final MkGithub github = new MkGithub();
        final Errors.Github errors = new Errors.Github(
            new Errors(new ExtDynamo().value()),
            github
        );
        final Repo repo = github.repos()
            .create(new Repos.RepoCreate("test", false));
        final Issue issue = repo.issues()
            .create("A bug", "RuntimeException in main()");
        final Comment comment = issue.comments().post("error");
        Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
        final Comment deleted = issue.comments().post("to-delete");
        errors.add(comment);
        errors.add(deleted);
        MatcherAssert.assertThat(
            "Error comment was not found",
            errors.iterate(2, 0L),
            Matchers.not(Matchers.emptyIterable())
        );
    }
}
