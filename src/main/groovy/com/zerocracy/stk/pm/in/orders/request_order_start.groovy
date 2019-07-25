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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs
import com.zerocracy.radars.github.Job

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Request order start')
  new Assume(project, xml).roles('ARC', 'PO')

  Farm farm = binding.variables.farm
  ClaimIn claim = new ClaimIn(xml)
  String login = claim.param('login')
  String job = claim.param('job')
  if (job.startsWith('gh:')) {
    Issue issue = new Issue.Smart(new Job.Issue(new ExtGithub(farm).value(), job))
    if (!issue.open) {
      throw new SoftException(
        new Par(farm, 'Job %s is closed, can\'t start order').say(job)
      )
    }
  }

  Wbs wbs = new Wbs(project).bootstrap()
  if (!wbs.exists(job)) {
    String role = 'DEV'
    if (claim.hasParam('role')) {
      role = claim.param('role')
    }
    if (job.startsWith('gh:')) {
      Issue issue = new Issue.Smart(new Job.Issue(new ExtGithub(farm).value(), job))
      if (issue.pull) {
        role = 'REV'
      }
    }
    wbs.add(job)
    wbs.role(job, role)
    claim.copy()
      .type('Job was added to WBS')
      .param('reason', 'Order start requested, but WBS is empty')
      .postTo(new ClaimsOf(farm, project))
  }
  Orders orders = new Orders(farm, project).bootstrap()
  if (orders.assigned(job)) {
    String performer = orders.performer(job)
    if (login == performer) {
      throw new SoftException(
        new Par(
          'Job %s is already assigned to @%s'
        ).say(job, login)
      )
    }
    orders.resign(job)
    claim.copy()
      .type('Order was canceled')
      .param('login', performer)
      .postTo(new ClaimsOf(farm, project))
  }
  claim.copy()
    .type('Start order')
    .author(claim.author())
    .param('login', login)
    .param('manual', true)
    .param('reason', claim.cid())
    .postTo(new ClaimsOf(farm, project))
}
