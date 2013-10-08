<%@ page contentType="text/html; charset=UTF-8" %>
<%--
  ~ Copyright 2011 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<!DOCTYPE html>
<html>
<head>
<title>Teams Mock for Group Authorisations</title>
</head>

<body>
This is the mock page for Group Authorisations
<br>
<form action="createGroup.html" method="POST">
<table>
<tr>
  <td>groupName</td>
  <td><input type="text" name="groupName" /></td>
</tr>
<tr>
  <td>licenseNumber</td>
  <td><input type="text" name="licenseNumber" /></td>
</tr>
<tr>
  <td>Quantity</td>
  <td><input type="text" name="quantity" /></td>
</tr>
<tr>
  <td colspan=2 align=right><input type="submit" value="createGroup"/></td>
</tr>
</table>
</form>
</body>
</html>