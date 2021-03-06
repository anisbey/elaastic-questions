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
<%@ page import="org.tsaap.assignments.InteractionType" %>
<%@ page import="org.tsaap.assignments.StateType" %>


<g:render template="/assignment/player/sequence/steps/steps-elaastic"
          model="[sequence: sequenceInstance,
                  stateByInteractionType: [
                      (InteractionType.ResponseSubmission): StateType.beforeStart.name(),
                      (InteractionType.Evaluation): StateType.beforeStart.name(),
                      (InteractionType.Read): StateType.beforeStart.name(),
                  ]]"/>

<div class="ui bottom attached warning message">
  ${message(code:"player.sequence.beforeStart.message")}
  <g:remoteLink controller="player"
                action="updateSequenceDisplay"
                id="${sequenceInstance.id}"
                title="Refresh"
                update="sequence_${sequenceInstance.id}"
                onComplete="MathJax.Hub.Queue(['Typeset',MathJax.Hub,'sequence_${sequenceInstance.id}'])">
    <i class="refresh icon"></i>
  </g:remoteLink>
</div>

<g:render template="/assignment/player/statement/show-elaastic"
          model="[statementInstance: sequenceInstance.statement, hideStatement: true]" />
