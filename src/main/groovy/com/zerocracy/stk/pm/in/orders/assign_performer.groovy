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

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pm.scope.Wbs

/**
 * Assign elected performer.
 *
 * @param project Current project
 * @param xml Claim
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Performer was elected')
  ClaimIn claim = new ClaimIn(xml)
  Wbs wbs = new Wbs(project).bootstrap()
  Collection<String> orders = new Orders(farm, project).bootstrap().iterate()
  Collection<String> reviews = new Reviews(project).bootstrap().iterate()
  Farm farm = binding.variables.farm
  String job = claim.param('job')
  String login = claim.param('login')
  if (!wbs.exists(job)) {
    return
  }
  if (orders.contains(job) || reviews.contains(job)) {
    return
  }
  claim.copy()
    .type('Start order')
    .token("job;${job}")
    .param('job', job)
    .param('login', login)
    .param('reason', claim.param('reason'))
    .param('public', true)
    .postTo(new ClaimsOf(farm, project))
}

