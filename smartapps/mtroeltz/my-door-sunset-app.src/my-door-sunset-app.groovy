/**
 *  My Door Sunset app
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
    name: "My Door Sunset app",
    namespace: "mtroeltz",
    author: "Michelle Troeltzsch",
    description: "Turns on a switch when a door opens between sunset and sunrise",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select SmartThings") {
        input "openCloseSensor", "capability.contactSensor", title: "Which door?", required: true, multiple: false
        input "roomLight", "capability.switch", title: "Which room light?", required: true, multiple: false
    }
    section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Zip code (optional, defaults to location coordinates when location services are enabled)...") {
		input "zipCode", "text", title: "Zip code", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	initialize()
    sendNotificationEvent("$app.name: settings updated")
}

def initialize() {
        log.debug "initialize()"
		subscribe(location, "position", locationPositionChange)
		subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
		subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
        sendNotificationEvent("$app.name: calling astrocheck from initialize")
		astroCheck()
        subscribe(openCloseSensor, "contact.open", contactHandler)
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	state.lastSunriseSunsetEvent = now()
	log.debug "$app.name: sunriseSunsetTimeHandler"
    sendNotificationEvent("$app.name: calling astrocheck from sunriseSunsetTimeHandler")
	astroCheck()
}

//  Event handlers

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

def contactHandler(evt) {
    // Door is opened. Now check if the current time is within the sunset to sunrise window
    if (enabled()) {
        roomLight.on()
        sendNotificationEvent("My Door Sunset app: light on")
    } else {
        roomLight.off()
        sendNotificationEvent("My Door Sunset app: light off")
    }
}

private enabled() {
	def result
	
    log.debug "enabled: checking time"
    def t = now()
    result = t < state.riseTime || t > state.setTime
        if ( t < state.riseTime) {
        	log.debug "less rise: $t < $state.riseTime"
        }
        if ( t > state.setTime) {
        	log.debug "greater set: $t > $state.setTime"
        }
        log.debug "enabled: $result"

	result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}