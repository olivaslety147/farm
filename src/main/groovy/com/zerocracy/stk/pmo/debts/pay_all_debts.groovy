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
package com.zerocracy.stk.pmo.debts

import com.jcabi.xml.XML
import com.jcabi.xml.XMLDocument
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Policy
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.farm.fake.FkProject
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Debts
import com.zerocracy.pmo.People
import com.zerocracy.pmo.banks.Payroll
import org.xembly.Xembler

import java.time.Duration
import java.time.Instant

def exec(Project pmo, XML xml) {
  /*
   * @todo #696:30min Add test for pay_all_depts stakeholder script.
   *  It is not possible because payments are not testable. It should be
   *  fixed in #565 ticket.
   */
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping hourly', 'Ping daily')
  Farm farm = binding.variables.farm
  Debts debts = new Debts(farm).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  boolean daily = claim.type().equalsIgnoreCase('ping daily')
  String filter
  if (daily) {
    filter = 'failure/attempt > 10'
  } else {
    filter = 'not(failure) or failure/attempt <= 10'
  }
  debts.iterate(filter).each { uid ->
    boolean  zld = new People(farm).bootstrap().bank(uid) == 'zld'
    if (!debts.expired(uid) && !zld) {
      return
    }
    Cash debt = debts.amount(uid)
    Policy policy = new Policy()
    if (!zld && debt < policy.get('46.threshold', new Cash.S('$50')) &&
      !debts.olderThan(uid, Instant.now() - Duration.ofDays(policy.get('46.days', 20)))) {
      return
    }
    try {
      Collection<String> amounts = new XMLDocument(
        new Xembler(debts.toXembly(uid)).xmlQuietly()
      ).xpath('//item/amount/text()')
      if (amounts.size() > 10) {
        amounts = amounts.subList(0, 9)
        amounts.add('...')
      }
      String details = amounts.join(', ')
      String pid = new Payroll(farm).pay(
        new Ledger(farm, new FkProject()).bootstrap(),
        uid, debt,
        new Par('Debt repayment, per §46: %s').say(details),
        debts.hash(uid)
      )
      debts.remove(uid)
      claim.copy()
        .type('Debt was paid')
        .param('login', uid)
        .param('payment_id', pid)
        .param('details', details)
        .param('amount', debt)
        .postTo(new ClaimsOf(farm))
      claim.copy()
        .type('Notify user')
        .token("user;${uid}")
        .param(
          'message',
          new Par(
            'We just paid you the debt of %s (`%s`)'
          ).say(debt, pid)
        )
        .postTo(new ClaimsOf(farm))
    } catch (IOException ex) {
      debts.failure(uid, ex.message)
      claim.copy()
        .type('Debt payment failed')
        .param('login', uid)
        .param('amount', debt)
        .param('failure', ex.message)
        .postTo(new ClaimsOf(farm))
      claim.copy()
        .type('Notify user')
        .token("user;${uid}")
        .param(
          'message',
          new Par(
            'We tried to pay your debt of %s, but failed (%s);',
            'don\'t worry, we will retry very soon'
          ).say(debt, ex.message)
        )
        .postTo(new ClaimsOf(farm))
      claim.copy().type('Notify PMO').param(
        'message',
        new Par(
          'We failed to pay the debt of %s to @%s: %s'
        ).say(debt, uid, ex.message)
      ).postTo(new ClaimsOf(farm))
      throw ex
    }
  }
}
