%{--
  -
  -  Copyright (C) 2017 Ticetime
  -
  -      This program is free software: you can redistribute it and/or modify
  -      it under the terms of the GNU Affero General Public License as published by
  -      the Free Software Foundation, either version 3 of the License, or
  -      (at your option) any later version.
  -
  -      This program is distributed in the hope that it will be useful,
  -      but WITHOUT ANY WARRANTY; without even the implied warranty of
  -      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -      GNU Affero General Public License for more details.
  -
  -      You should have received a copy of the GNU Affero General Public License
  -      along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -
  --}%


<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>${message(code: "elaastic.terms.title")}</title>
  <meta name="layout" content="anonymous-elaastic">
  <r:require modules="semantic_ui,elaastic_ui,jquery"/>
</head>

<body>

<div class="ui icon message">
  <i class="law icon"></i>

  <div class="content">
    <div class="header">
      ${message(code: "elaastic.terms.title")}
    </div>

    <p>${message(code: "elaastic.terms")}</p>
  </div>
</div>


<div class="ui segment">
  <h3 class="ui header">${message(code: "elaastic.terms.question")}</h3>
  <g:link class="ui primary button" controller="userAccount" action="ltiConnection"
          params='[agree: "true", assignment_id: params.assignment_id, username: params.username, contextId: params.contextId, contextName: params.contextName, displaysAll: "on"]'>${message(code: "elaastic.terms.agree")}</g:link>
  <g:link class="ui button" controller="userAccount" action="ltiConnection"
          params='[agree: "false"]'>${message(code: "elaastic.terms.disagree")}</g:link>

</div>
</body>
</html>