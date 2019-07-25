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
package com.zerocracy.stk.pm.cost.funding

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Catalog

/**
 * This stakeholder is called when project is funded by Stripe,
 * PO can fund it directly from project page in
 * {@link com.zerocracy.tk.project.TkStripePay}
 * or it can be funded automatically due to
 * {@link com.zerocracy.pmo.recharge.Recharge} mechanism.
 *
 * @param project Funded project
 * @param xml Claim
 *
 */
def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Funded by Stripe')
  ClaimIn claim = new ClaimIn(xml)
  Cash amount = new Cash.S(claim.param('amount'))
  String pid = claim.param('payment_id')
  String details
  if (claim.hasAuthor()) {
    details = new Par(
      'Funded via Stripe by @%s, payment ID is `%s`'
    ).say(claim.author(), pid)
  } else {
    details = new Par(
      'Funded via Stripe by recharge, payment ID is `%s`'
    ).say(pid)
  }
  new Ledger(farm, project).bootstrap().add(
    new Ledger.Transaction(
      amount,
      'assets', 'cash',
      'income', claim.param('stripe_customer'),
      details
    )
  )
  Farm farm = binding.variables.farm
  claim.copy()
    .type('Notify project')
    .param(
      'message',
      new Par(
        farm,
        'The project %s has been funded via Stripe for %s;',
        'payment ID is `%s`;',
        'we will re-charge the card automatically for the same amount',
        'when the project runs out of funds;',
        'to stop that just put the project on pause, see §21'
      ).say(project.pid(), amount, pid)
    )
    .postTo(new ClaimsOf(farm, project))
  claim.copy().type('Notify PMO').param(
    'message', new Par(
      farm,
      'We just funded %s for %s via Stripe'
    ).say(project.pid(), amount)
  ).postTo(new ClaimsOf(farm, project))
  Catalog catalog = new Catalog(farm).bootstrap()
  if (catalog.hasAdviser(project.pid())) {
    // @todo #1658:30min Add adviser info to policy and provide amount
    //  of adviser payments there, then replace current constants which are used
    //  to calculate the payment with `Par` objects.
    Cash bonus = amount.mul(4) / 100
    String adviser = catalog.adviser(project.pid())
    new ClaimOut()
      .type('Make payment')
      .param('login', adviser)
      .param('job', 'none')
      .param('cash', bonus)
      .param(
      'reason',
        new Par(farm, 'Adviser payment for @%s project (%s)')
          .say(project.pid(), pid)
      ).postTo(new ClaimsOf(farm))
    new ClaimOut().type('Notify PMO').param(
      'message',
      new Par(farm, 'We just send adviser payment of %s for %s to %s').say(bonus, project.pid(), adviser)
    ).postTo(new ClaimsOf(farm))
  }
  if (catalog.sandbox(project.pid()) && claim.hasAuthor()) {
    claim.copy().type('Send zold')
      .param('recipient', claim.author())
      .param('reason', 'Funded reward')
      .postTo(new ClaimsOf(farm, project))
  }
}
