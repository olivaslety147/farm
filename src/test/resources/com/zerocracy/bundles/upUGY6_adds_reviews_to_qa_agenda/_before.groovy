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
package com.zerocracy.bundles.adds_reviews_to_qa_agenda

import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.entry.ExtGithub
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pmo.Agenda

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new ExtGithub(farm).value().repos()
    .create(new Repos.RepoCreate('test', false))
    .issues().create('hello1', 'world1')
  new Agenda(farm, 'ypshenychka').bootstrap().add(project, 'gh:test/test#1', 'QA')
  new Reviews(project).bootstrap()
    .add('gh:test/test#2','ypshenychka', 'krzyk', new Cash.S('$10'), 30, new Cash.S('$0'))
}
