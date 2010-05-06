<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:output method="xml" version="1.0" encoding="utf8"
		indent="yes" />

	<xsl:template match="/">
		<xsl:apply-templates select="*" />
	</xsl:template>

	<xsl:template match="OAuthConfig">
		<html>
			<head>
				<title>OAuth Proxy Config</title>
			</head>
			<body>
				<h1>OAuth Proxy Config</h1>
				<hr />
				<h2>Consumer</h2>
				<form id="ConsumerAddForm" method="get" action="#">
					<table border="1">
						<tr>
							<th width="20" />
							<th width="250">Key</th>
							<th width="100">Method</th>
							<th width="500">Secret</th>
							<th width="250">Replace With Consumer</th>
						</tr>
						<xsl:apply-templates select="consumers" />
						<tr>
							<input type="hidden" name="action" value="ConsumerAdd"
								hidden="true" />
							<td>
								<input type="submit" value="+" />
							</td>
							<td>
								<input name="key" style="width:98%" />
							</td>
							<td>
								<select name="method" style="width:95%">
									<option>HMAC-SHA1</option>
									<option>RSA-SHA1</option>
									<option>PLAINTEXT</option>
								</select>
							</td>
							<td>
								<input name="secret" style="width:99%" />
							</td>
							<td>
								<input name="replace" style="width:99%" />
							</td>
						</tr>
					</table>
				</form>
				<hr />
				<h2>Users</h2>
				<table border="1">
					<tr>
						<th width="20" />
						<th width="500">Token</th>
						<th width="200">Secret</th>
					</tr>
					<xsl:apply-templates select="users" />
				</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="consumers">
		<tr>
			<td>
				<b>
					<button type="button">
						<xsl:attribute name="onclick">
							<xsl:text>location.href='config?action=ConsumerDel&amp;key=</xsl:text>
							<xsl:value-of select="@Key" />
							<xsl:text>';return false;</xsl:text>
						</xsl:attribute>
						<xsl:text>-</xsl:text>
					</button>
				</b>
			</td>
			<td>
				<xsl:value-of select="@Key" />
			</td>
			<td>
				<xsl:value-of select="Method" />
			</td>
			<td>
				<xsl:value-of select="Secret" />
			</td>
			<td>
				<xsl:value-of select="@ReplaceKey" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="users">
		<tr>
			<td>
				<b>
					<button type="button">
						<xsl:attribute name="onclick">
							<xsl:text>location.href='config?action=UserDel&amp;token=</xsl:text>
							<xsl:value-of select="Token" />
							<xsl:text>';return false;</xsl:text>
						</xsl:attribute>
						<xsl:text>-</xsl:text>
					</button>
				</b>
			</td>
			<td>
				<xsl:value-of select="Token" />
			</td>
			<td>
				<xsl:value-of select="Secret" />
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
