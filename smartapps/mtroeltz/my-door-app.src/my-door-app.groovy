/**
 *  My Door App
 *
 *  Copyright 2018 Michelle Troeltzsch
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My Door App",
    namespace: "mtroeltz",
    author: "Michelle Troeltzsch",
    description: "Playing with turning on a switch when a door is opened during certain times of day.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Select SmartThings") {
        input "openCloseSensor", "capability.contactSensor", title: "Which door?", required: true, multiple: false
        input "roomLight", "capability.switch", title: "Which room light?", required: true, multiple: false
    }
    section("Turn on between what times?") {
        input "fromTime", "time", title: "From", required: true
        input "toTime", "time", title: "To", required: true
    } 
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
    
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
    log.debug "initializing the contactHandler"
    subscribe(openCloseSensor, "contact.open", contactHandler)
}


// Event Handlers

def contactHandler(evt) {

    // Door is opened. Now check if the current time is within the visiting hours window
    
    log.debug "fromTime: $fromTime"
    log.debug "toTime: $toTime"
    //log.debug "location timezone: $location.timeZone"
    // use this for the simulator because on Wenny the timeZone doesn't exit.
    //def between = timeOfDayIsBetween(fromTime, toTime, new Date(), TimeZone.getTimeZone("MST"))
    def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
    log.debug "between is $between"
    if (between) {
        roomLight.on()
    } else {
        roomLight.off()
    }
}
