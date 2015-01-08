package org.tsaap.questions

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gcontracts.annotations.Requires
import org.tsaap.directory.User
import org.tsaap.ia.conflict.SocioCognitiveConflictService

class SessionPhase {

    public static final int MAX_RANK = 3

    Date dateCreated
    String status = LiveSessionStatus.NotStarted.name()
    Date startDate
    Date endDate
    String resultMatrixAsJson
    Integer rank

    LiveSession liveSession

    static constraints = {
        status inList: LiveSessionStatus.values()*.name()
        startDate nullable: true
        endDate nullable: true
        resultMatrixAsJson nullable: true
    }


    /**
     * Flag to indicate if the phase is not started
     * @return true if the phase is not started,false otherwise
     */
    boolean isNotStarted() {
        status == LiveSessionStatus.NotStarted.name()
    }

    /**
     * Flag to indicate if the phase is started
     * @return true if the phase is started,false otherwise
     */
    boolean isStarted() {
        status == LiveSessionStatus.Started.name()
    }

    /**
     * Flag to indicate if the phase is stopped
     * @return true if the phase is stopped,false otherwise
     */
    boolean isStopped() {
        status == LiveSessionStatus.Ended.name()
    }

    /**
     * Start the current phase
     */
    @Requires({ isNotStarted() })
    def start() {
        status = LiveSessionStatus.Started.name()
        startDate = new Date()
        save(flush: true)
    }

    /**
     * Stop the current phase
     */
    @Requires({ isStarted() })
    def stop(boolean shouldBuildResultMatrix = true) {
        status = LiveSessionStatus.Ended.name()
        endDate = new Date()
        if (shouldBuildResultMatrix) {
           updateResultMatrixAsJson()
        }
        save(flush: true)
    }

    private def void updateResultMatrixAsJson() {
        def matrix = buildResultMatrix()
        JsonBuilder builder = new JsonBuilder(matrix ?: [:])
        resultMatrixAsJson = builder.toString()
    }

    /**
     * Get the response of the current session phase for a given user
     * @param user the given user
     * @return the live session response if it exists
     */
    LiveSessionResponse getResponseForUser(User user) {
        LiveSessionResponse.findBySessionPhaseAndUser(this, user,[fetch: [explanation: 'join'] ])
    }

    /**
     * get the count of responses for the current live session
     * @return the count of responses
     */
    Integer responseCount() {
        LiveSessionResponse.countBySessionPhase(this)
    }

    /**
     * Construct the result matrix of the current live session
     * @return the result matrix
     */
    List<Map<String, Float>> getResultMatrix() {
        JsonSlurper parser = new JsonSlurper()
        if (resultMatrixAsJson == null) {
            updateResultMatrixAsJson()
            save(flush: true)
        }
        def matrix = parser.parseText(resultMatrixAsJson)
        matrix
    }

    ResultMatrixService resultMatrixService

    /**
     * Construct the result matrix of the current live session
     * @return the result matrix
     */
    List<Map<String, Float>> buildResultMatrix() {
        def responses = LiveSessionResponse.findAllBySessionPhase(this)
        Question question = this.liveSession.note.question
        resultMatrixService.buildResultMatrixForQuestionAndResponses(question,responses)
    }

    /**
     *
     * @return true if have to stop the live session when this phase is stopped
     */
    boolean stopLiveSessionWhenIsStopped() {
        rank == MAX_RANK
    }

    SocioCognitiveConflictService socioCognitiveConflictService

    /**
     *
     * @param response the response to find the conflict response
     * @return the conflict response
     */
    LiveSessionResponse findConflictResponseForResponse(LiveSessionResponse response) {
        def responseList = LiveSessionResponse.findAllBySessionPhase(this)
        socioCognitiveConflictService.findResponseInResponseListWithBestConflictWithResponse(
                responseList,response)
    }

    List<LiveSessionResponse> findAllResponsesToEvaluateForResponse(LiveSessionResponse response) {
        def responseList = LiveSessionResponse.findAllBySessionPhase(this)
        socioCognitiveConflictService.findAllResponseToEvaluateInResponseListForResponse(responseList,response)
    }

    static transients = ['resultMatrix', 'resultMatrixService','MAX_RANK','socioCognitiveConflictService']
}
