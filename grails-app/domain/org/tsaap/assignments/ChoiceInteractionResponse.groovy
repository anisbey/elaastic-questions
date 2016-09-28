package org.tsaap.assignments

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.tsaap.assignments.interactions.InteractionChoice
import org.tsaap.assignments.interactions.ResponseSubmissionSpecification
import org.tsaap.directory.User

class ChoiceInteractionResponse {

    Date dateCreated
    Date lastUpdated

    User learner
    Interaction interaction

    String choiceListSpecification
    String explanation
    Integer confidenceDegree

    Float score

    static constraints = {
        score nullable: true
        choiceListSpecification nullable: true
        explanation nullable: true
        confidenceDegree nullable: true
    }

    /**
     * Get the choice list
     * @return the choice list
     */
    List<Integer> choiceList() {
        if (!choiceListSpecification) {
            return []
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        jsonSlurper.parseText(choiceListSpecification)
    }

    /**
     * Update the choice list specification
     * @param choiceList the input choice list
     */
    void updateChoiceListSpecification(List<Integer> choiceList) {
        choiceListSpecification = JsonOutput.toJson(choiceList)
    }


    /**
     * Calculate and update the score property
     * @return the score value
     */
    Float updateScore() {
        ResponseSubmissionSpecification spec = interaction.interactionSpecification
        Float res = 0
        for(InteractionChoice choice in spec.expectedChoiceList) {
            if (choiceList().contains(choice.index)) {
                res += choice.score
            }
        }
        score = res
        score
    }

    /**
     * Get the assignment relative to this response
     * @return the assignment
     */
    Assignment assignment() {
        interaction.sequence.assignment
    }
}

enum ConfidenceDegreeEnum {
    NOT_CONFIDENT_AT_ALL,
    NOT_REALLY_CONFIDENT,
    CONFIDENT,
    TOTALLY_CONFIDENT

    String getName() {
        name()
    }

    String getIntegerValue() {
        ordinal()
    }
}