/**
 *  Wenny&rsquo;s First App
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
    name: "Wenny Cat Emily",
    namespace: "Wennydraggon ",
    author: "Michelle Troeltzsch",
    description: "This is my first app walking through the tutorial",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn on when motion detected:") {
        input "themotion", "capability.motionSensor", required: true, title: "Where?"
        
    }

    section("Turn on this light") {
        input "theswitch", "capability.switch", required: true
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
	// TODO: subscribe to attributes, devices, locations, etc.    
    log.debug "initialize in wenny cat emily"
    subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}   

// TODO: implement event handlers
def motionDetectedHandler(evt) {
    log.debug "a motionDetectedHandler called: $evt"
    theswitch.on();
    log.info "switch is on"
}

def motionStoppedHandler(evt) {
    log.debug "motionStoppedHandler called: $evt"
    theswitch.off();
    log.info "switch is off"
}


