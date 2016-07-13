%{--
  - Copyright (C) 2013-2016 Université Toulouse 3 Paul Sabatier
  -
  -     This program is free software: you can redistribute it and/or modify
  -     it under the terms of the GNU Affero General Public License as published by
  -     the Free Software Foundation, either version 3 of the License, or
  -     (at your option) any later version.
  -
  -     This program is distributed in the hope that it will be useful,
  -     but WITHOUT ANY WARRANTY; without even the implied warranty of
  -     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -     GNU Affero General Public License for more details.
  -
  -     You should have received a copy of the GNU Affero General Public License
  -     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%

<g:set var="idControllSuffix" value="${parentNote ? parentNote.id : 0}${note ? "_" + note.id : ""}"/>

<div class="panel-body">
    <g:form method="post" action="${note ? 'update' : 'add'}"
            enctype="multipart/form-data">
        <g:if test="${note}">
            <g:hiddenField name="noteId" value="${note.id}"/>
        </g:if>
        <g:hiddenField name="inline" value="${params.inline}"/>
        <g:hiddenField name="contextId" value="${context?.id}"
                       id="contextIdInAddForm${idControllSuffix}"/>
        <g:hiddenField name="fragmentTagId" value="${fragmentTag?.id}"/>
        <g:hiddenField name="parentNoteId" value="${parentNote?.id}"/>
        <g:hiddenField name="displaysMyNotes" id="displaysMyNotesInAddForm${idControllSuffix}"/>
        <g:hiddenField name="displaysMyFavorites"
                       id="displaysMyFavoritesInAddForm${idControllSuffix}"/>
        <g:hiddenField name="displaysAll" id="displaysAllInAddForm${idControllSuffix}"/>
        <a id="question_sample${idControllSuffix}"
           style="margin-top: 15px">${message(code: "notes.edit.sampleQuestion")}</a>
        <textarea class="form-control note-editable-content" rows="3"
                  id="noteContent${idControllSuffix}"
                  name="noteContent">${note ? note.content : ""}</textarea>

        <div class="row">
            <div class="btn-toolbar">
                <div id="attach${idControllSuffix}" class="col-sm-8 col-md-8 col-lg-8">
                    <g:set var="attachment"/>
                    <g:if test="${note}">
                        <g:set var="attachment" value="${note.attachment}"/>
                        <g:if test="${attachment != null}">
                            <tsaap:viewAttachement width="150" height="150" attachement="${attachment}"/>
                            <g:remoteLink controller="notes" action="removeAttachement"
                                          params="[noteId: note.id]"
                                          update="attach${idControllSuffix}">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                            </g:remoteLink>
                        </g:if>
                    </g:if>
                    <g:if test="${!attachment}">
                        <input type="file" name="myFile" title="Image: gif, jpeg and png only"/>
                    </g:if>
                </div>
                <div class="col-sm-4 col-md-4 col-lg-4">
                    <div class="pull-right">
                        <button type="button"
                                class="btn btn-default btn-xs"
                                id="preview_button_${idControllSuffix}">
                            ${message(code: "notes.edit.preview")}
                        </button>

                        <button type="submit"
                                class="btn btn-primary btn-xs"
                                id="buttonAddNote${idControllSuffix}"
                                disabled>
                            <g:if test="${note}">
                                ${message(code: "notes.edit.update.question.button")}
                            </g:if>
                            <g:else>
                                <span class="glyphicon glyphicon-plus"></span> ${message(code: "notes.edit.add.question.button")}
                            </g:else>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</div>

<r:script>
$(document).ready(function () {
    var contentPreview;

    // preview management
    //--------
    $('#preview_button_${idControllSuffix}').popover({
        content: function() {return getNotePreview()},
        html: true,
        placement: 'bottom'
    }).on('shown.bs.popover', function() {
        MathJax.Hub.Queue(['Typeset',MathJax.Hub,'prewiew_tab_${idControllSuffix}']);
    });

    function getNotePreview() {
        var noteInput = $("#noteContent${idControllSuffix}").val();
        contentPreview = noteInput;
        if (noteInput.lastIndexOf('::', 0) === 0) {
            $.ajax({
                type: "POST",
                url: '<g:createLink action="evaluateContentAsQuestion"/>',
                data: {content:noteInput},
                async: false
            }).done(function(data) {
                contentPreview = data ;
            });
        }
        return contentPreview;
    }

    // set character counters
    //-----------------------

    // Get the textarea field
    $("#noteContent${idControllSuffix}")

        // Bind the counter function on keyup and blur events
        .bind('keyup blur', function () {
            // Count the characters and set the counter text
            var counter =  $("#character_counter${idControllSuffix}");
            counter.text($(this).val().length + '/560 ${message(code: "notes.edit.characters")}');
            if ($(this).val().length >1) {
                $("#buttonAddNote${idControllSuffix}").removeAttr('disabled');
            } else {
                $("#buttonAddNote${idControllSuffix}").attr('disabled','disabled');
            }
        })

        // Trigger the counter on first load
        .keyup();

    // set hidden field value
    //----------------------
    $("#displaysMyNotesInAddForm${idControllSuffix}").val($("#displaysMyNotes").attr('checked') ? 'on' : '');
    $("#displaysMyFavoritesInAddForm${idControllSuffix}").val($("#displaysMyFavorites").attr('checked') ? 'on' : '');
    $("#displaysAllInAddForm${idControllSuffix}").val($("#displaysAll").attr('checked') ? 'on' : '');

    // Questions samples popup management
    $('#question_sample${idControllSuffix}').popover({
        content: function() {return getQuestionSample${idControllSuffix}()},
        html: true,
        placement: 'bottom'
    }).on('shown.bs.popover', function() {
        MathJax.Hub.Queue(['Typeset',MathJax.Hub,'question_sample'])
    });

    function ${'getQuestionSample' + idControllSuffix}() {
        var contentQuestionSample = "";
    $.ajax({
        type: "POST",
        url: '<g:createLink action="getSamples"
                            params="[questionSample: 'question_sample' + idControllSuffix, toUpdate: 'noteContent' + idControllSuffix]"/>',
        async: false
    }).done(function(data) {
        contentQuestionSample = data;
    });
        return contentQuestionSample;
    }

});

function sampleLink(id, questionSample, toUpdate){
    $('#' + questionSample).popover('hide');
    var precedentText = $('#' + toUpdate).val();
    if(id == 0) {
        $('#' + toUpdate).val("${message(code: "notes.edit.sampleQuestion.singleChoiceExemple")}"+"\n"+precedentText);
    }
    else {
        $('#' + toUpdate).val("${message(code: "notes.edit.sampleQuestion.multipleChoiceExemple")}" +"\n"+precedentText);
    }
    $('#' + toUpdate).focus();
    $('#' + toUpdate).blur();
}
</r:script>