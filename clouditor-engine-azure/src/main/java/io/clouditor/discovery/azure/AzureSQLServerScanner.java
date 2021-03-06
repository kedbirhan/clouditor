/*
 * Copyright 2016-2019 Fraunhofer AISEC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *            $$\                           $$\ $$\   $$\
 *            $$ |                          $$ |\__|  $$ |
 *   $$$$$$$\ $$ | $$$$$$\  $$\   $$\  $$$$$$$ |$$\ $$$$$$\    $$$$$$\   $$$$$$\
 *  $$  _____|$$ |$$  __$$\ $$ |  $$ |$$  __$$ |$$ |\_$$  _|  $$  __$$\ $$  __$$\
 *  $$ /      $$ |$$ /  $$ |$$ |  $$ |$$ /  $$ |$$ |  $$ |    $$ /  $$ |$$ |  \__|
 *  $$ |      $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$\ $$ |  $$ |$$ |
 *  \$$$$$$\  $$ |\$$$$$   |\$$$$$   |\$$$$$$  |$$ |  \$$$   |\$$$$$   |$$ |
 *   \_______|\__| \______/  \______/  \_______|\__|   \____/  \______/ \__|
 *
 * This file is part of Clouditor Community Edition.
 */

package io.clouditor.discovery.azure;

import com.microsoft.azure.management.sql.SqlActiveDirectoryAdministrator;
import com.microsoft.azure.management.sql.SqlEncryptionProtector;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlServerSecurityAlertPolicy;
import com.microsoft.azure.management.sql.implementation.ServerBlobAuditingPolicyInner;
import io.clouditor.discovery.Asset;
import io.clouditor.discovery.ScanException;
import io.clouditor.discovery.ScannerInfo;
import java.util.List;

@ScannerInfo(assetType = "SQLServer", group = "Azure", service = "SQL", assetIcon = "fas fa-server")
public class AzureSQLServerScanner extends AzureScanner<SqlServer> {

  public AzureSQLServerScanner() {
    super(SqlServer::id, SqlServer::name);
  }

  @Override
  protected List<SqlServer> list() {
    return this.resourceGroup != null
        ? this.api.azure().sqlServers().listByResourceGroup(this.resourceGroup)
        : this.api.azure().sqlServers().list();
  }

  @Override
  protected Asset transform(SqlServer server) throws ScanException {
    var asset = super.transform(server);

    enrich(
        asset,
        "securityAlertPolicy",
        server,
        x -> x.serverSecurityAlertPolicies().get(),
        SqlServerSecurityAlertPolicy::id,
        SqlServerSecurityAlertPolicy::name);

    enrich(
        asset,
        "encryptionProtectors",
        server,
        x -> x.encryptionProtectors().get(),
        SqlEncryptionProtector::id,
        SqlEncryptionProtector::serverKeyName);

    enrich(
        asset,
        "activeDirectoryAdmin",
        server,
        x -> server.getActiveDirectoryAdministrator(),
        SqlActiveDirectoryAdministrator::id,
        SqlActiveDirectoryAdministrator::signInName);

    enrichList(
        asset,
        "firewallRules",
        server,
        x -> x.firewallRules().list(),
        SqlFirewallRule::id,
        SqlFirewallRule::name);

    enrich(
        asset,
        "auditingPolicy",
        server,
        x ->
            this.api
                .azure()
                .sqlServers()
                .manager()
                .inner()
                .serverBlobAuditingPolicies()
                .get(server.resourceGroupName(), server.name()),
        ServerBlobAuditingPolicyInner::id,
        ServerBlobAuditingPolicyInner::name);

    return asset;
  }
}
