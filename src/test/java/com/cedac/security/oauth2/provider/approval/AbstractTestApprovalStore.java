/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cedac.security.oauth2.provider.approval;

import org.junit.Test;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mauro.franceschini
 * @since 1.0.0
 */
public abstract class AbstractTestApprovalStore {

    protected abstract ApprovalStore getApprovalStore();

    protected boolean addApprovals(Collection<Approval> approvals) {
        return getApprovalStore().addApprovals(approvals);
    }

    @Test
    public void testAddEmptyCollection() {
        assertTrue(addApprovals(Arrays.<Approval>asList()));
        assertEquals(0, getApprovalStore().getApprovals("foo", "bar").size());
    }

    @Test
    public void testAddDifferentScopes() {
        assertTrue(addApprovals(
                Arrays.<Approval>asList(new Approval("user", "client", "read", 1000, ApprovalStatus.APPROVED),
                        new Approval("user", "client", "write", 1000, ApprovalStatus.APPROVED))));
        assertEquals(2, getApprovalStore().getApprovals("user", "client").size());
    }

    @Test
    public void testIdempotentAdd() {
        assertTrue(addApprovals(
                Arrays.<Approval>asList(new Approval("user", "client", "read", 1000, ApprovalStatus.APPROVED),
                        new Approval("user", "client", "write", 1000, ApprovalStatus.APPROVED))));
        assertTrue(addApprovals(
                Arrays.<Approval>asList(new Approval("user", "client", "read", 1000, ApprovalStatus.APPROVED),
                        new Approval("user", "client", "write", 1000, ApprovalStatus.APPROVED))));
        assertEquals(2, getApprovalStore().getApprovals("user", "client").size());
    }

    @Test
    public void testAddDifferentClients() {
        assertTrue(addApprovals(
                Arrays.<Approval>asList(new Approval("user", "client", "read", 1000, ApprovalStatus.APPROVED),
                        new Approval("user", "other", "write", 1000, ApprovalStatus.APPROVED))));
        assertEquals(1, getApprovalStore().getApprovals("user", "client").size());
        assertEquals(1, getApprovalStore().getApprovals("user", "other").size());
    }

    @Test
    public void testVanillaRevoke() {
        Approval approval1 = new Approval("user", "client", "read", 1000, ApprovalStatus.APPROVED);
        Approval approval2 = new Approval("user", "client", "write", 1000, ApprovalStatus.APPROVED);
        assertTrue(addApprovals(Arrays.<Approval>asList(approval1, approval2)));
        getApprovalStore().revokeApprovals(Arrays.asList(approval1));
        assertEquals(getExpectedNumberOfApprovalsAfterRevoke(), getApprovalStore().getApprovals("user", "client").size());
    }

    protected int getExpectedNumberOfApprovalsAfterRevoke() {
        return 1;
    }
}
