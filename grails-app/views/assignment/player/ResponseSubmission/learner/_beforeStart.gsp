<div class="alert alert-warning" role="alert">${message(code:"player.sequence.interaction.beforeStart.message", args:[interactionInstance.rank])} <g:remoteLink controller="player" action="updateSequenceDisplay" id="${interactionInstance.sequenceId}" title="Refresh" update="sequence_${interactionInstance.sequenceId}"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></g:remoteLink></div>