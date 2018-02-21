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

<div class="ui message" style="font-size: 1rem;">
<g:message code="player.sequence.interaction.responseCount" args="[1]"/>
<span id="response_count_${interactionInstance.id}">
  ${interactionInstance.interactionResponseCount()}
</span>
<g:remoteLink controller="player"
              action="updateInteractionResponseCount"
              id="${interactionInstance.id}"
              title="Refresh"
              update="response_count_${interactionInstance.id}">
  <i class="refresh icon"></i>
</g:remoteLink>
</div>

<g:remoteLink class="ui primary button" controller="player" action="stopInteraction" id="${interactionInstance.id}"
              update="sequence_${interactionInstance.sequenceId}">
  <i class="stop icon"></i>
  ${message(code: "player.sequence.interaction.stop", args: [interactionInstance.rank])}
</g:remoteLink>