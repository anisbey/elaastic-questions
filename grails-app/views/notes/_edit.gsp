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

<g:set var="idControllSuffix" value="${parentNote ? parentNote.id : 0}"/>

<div id="edit_tab_${idControllSuffix}">
    <g:if test="${params.kind == 'question'}">
        <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
            <div class="panel panel-default">
                <div class="panel-heading" role="tab" id="headingOne" style="margin-bottom: -15px;">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" id="accordionLink" data-parent="#accordion" href="#collapseOne"
                           aria-expanded="false" aria-controls="collapseOne" class="collapsed">
                            ${message(code: "notes.edit.question.editor")}
                        </a>
                    </h4>
                </div>

                <div id="collapseOne" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingOne">
                    <div class="panel-body">
                        <g:form method="post" controller="notes" action="addNote" enctype="multipart/form-data">
                            <g:hiddenField name="kind" value="${params.kind}"/>
                            <g:hiddenField name="inline" value="${params.inline}"/>
                            <g:hiddenField name="contextId" value="${context?.id}"
                                           id="contextIdInAddForm${idControllSuffix}"/>
                            <g:hiddenField name="fragmentTagId" value="${fragmentTag?.id}"/>
                            <g:hiddenField name="parentNoteId" value="${parentNote?.id}"/>
                            <g:hiddenField name="displaysMyNotes" id="displaysMyNotesInAddForm${idControllSuffix}"/>
                            <g:hiddenField name="displaysMyFavorites"
                                           id="displaysMyFavoritesInAddForm${idControllSuffix}"/>
                            <g:hiddenField name="displaysAll" id="displaysAllInAddForm${idControllSuffix}"/>
                            <g:set var="kind" value="question"/>
                            <a id="question_sample"
                               style="margin-top: 15px">${message(code: "notes.edit.sampleQuestion")}</a>
                            <textarea class="form-control note-editable-content" rows="3"
                                      id="noteContent${idControllSuffix}"
                                      name="noteContent">${parentNote ? '@' + parentNote.author?.username + ' ' : ''}</textarea>

                            <div class="row">
                                <div class="col-sm-7 col-md-7 col-lg-7">
                                    <input type="file" name="myFile" title="Image: gif, jpeg and png only"
                                           style="margin-top: 5px"/>
                                </div>

                                <div id="prewiew_tab_${idControllSuffix}" class="col-sm-2 col-md-2 col-lg-2">
                                    <button type="button" class="btn btn-default btn-xs"
                                            id="preview_button_${idControllSuffix}">
                                        ${message(code: "notes.edit.preview")}
                                    </button>
                                </div>

                                <div class="col-sm-2 col-md-2 col-lg-2">
                                    <button type="submit"
                                            class="btn btn-primary btn-xs"
                                            id="buttonAddNote${idControllSuffix}"
                                            disabled><span
                                            class="glyphicon glyphicon-plus"></span>${message(code: "notes.edit.add.question.button")}
                                    </button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    <g:else>
        <g:form method="post" controller="notes" action="addNote" enctype="multipart/form-data">
            <g:hiddenField name="kind" value="${params.kind}"/>
            <g:hiddenField name="inline" value="${params.inline}"/>
            <g:hiddenField name="contextId" value="${context?.id}"
                           id="contextIdInAddForm${idControllSuffix}"/>
            <g:hiddenField name="fragmentTagId" value="${fragmentTag?.id}"/>
            <g:hiddenField name="parentNoteId" value="${parentNote?.id}"/>
            <g:hiddenField name="displaysMyNotes" id="displaysMyNotesInAddForm${idControllSuffix}"/>
            <g:hiddenField name="displaysMyFavorites"
                           id="displaysMyFavoritesInAddForm${idControllSuffix}"/>
            <g:hiddenField name="displaysAll" id="displaysAllInAddForm${idControllSuffix}"/>
            <textarea class="form-control note-editable-content" rows="3" id="noteContent${idControllSuffix}"
                      name="noteContent"
                      maxlength="560">${parentNote ? '@' + parentNote.author?.username + ' ' : ''}</textarea>

            <div class="row">
                <span class="character_counter pull-left" style="margin-left: 15px"
                      id="character_counter${idControllSuffix}"></span>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-10 col-md-10 col-lg-10">
                    <input type="file" name="myFile" title="Image: gif, jpeg and png only"/>
                </div>

                <div class="col-xs-12 col-sm-2 col-md-2 col-lg-2">
                    <button type="submit"
                            class="btn btn-primary btn-xs"
                            id="buttonAddNote${idControllSuffix}"
                            disabled><span
                            class="glyphicon glyphicon-plus"></span>${message(code: "notes.edit.add.note.button")}
                    </button>
                </div>
            </div>
        </g:form>
    </g:else>
</div>


<r:script>
  $(document).ready(function () {

    $("#collapseOne").on('show.bs.collapse', function(){
        $("#headingOne").attr("style","margin-bottom: 0px;");
    });

    $("#collapseOne").on('hidden.bs.collapse', function(){
        $("#headingOne").attr("style","margin-bottom: -15px;");
    });

    var contentPreview;

    // preview management
    //--------
    $('#preview_button_${idControllSuffix}').popover({
                                 content: function() {return getNotePreview()},
                                 html: true,
                                 placement: 'bottom'
                               }).on('shown.bs.popover', function() {
                                 MathJax.Hub.Queue(['Typeset',MathJax.Hub,'prewiew_tab_${idControllSuffix}'])
                               });

    function getNotePreview() {
        var noteInput = $("#noteContent${idControllSuffix}").val() ;
        contentPreview = noteInput;
        if (noteInput.lastIndexOf('::', 0) === 0) {
            $.ajax({
                type: "POST",
                url: '<g:createLink action="evaluateContentAsNote" controller="notes"/>',
                data: {content:noteInput},
                async: false
            }).done(function( data ) {
                contentPreview = data ;
            });
         }
         return contentPreview
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
    $('#question_sample').popover({
                                 content: function() {return getQuestionSample()},
                                 html: true,
                                 placement: 'bottom'
                               }).on('shown.bs.popover', function() {
                                 MathJax.Hub.Queue(['Typeset',MathJax.Hub,'question_sample'])
                               });

    function getQuestionSample() {
        var contentQuestionSample = "" ;
            $.ajax({
                type: "POST",
                url: '<g:createLink action="getQuestionsSamples" controller="notes"/>',
                async: false
            }).done(function( data ) {
                contentQuestionSample = data ;
            });
         return contentQuestionSample
    }

    });

    function sampleLink(id){
        $('#question_sample').popover('hide');
        var precedentText = $('textarea[name="noteContent"]').val();
        if(id == 0) {
            $('textarea[name="noteContent"]').val("${message(code: "notes.edit.sampleQuestion.singleChoiceExemple")}"+"\n"+precedentText);
        }
        else {
            $('textarea[name="noteContent"]').val("${message(code: "notes.edit.sampleQuestion.multipleChoiceExemple")}" +"\n"+precedentText);
        }
        $('textarea[name="noteContent"]').focus();
        $('textarea[name="noteContent"]').blur()
    }

</r:script>