/**
 * Copyright (c) 2025 SRAM LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package io.hammerhead.karooext.models

import android.graphics.drawable.Drawable

/**
 * Derived from XML meta-data in [ExtensionInfo].
 */
data class DataType(
    val extension: String,
    val typeId: String,
    val displayName: String,
    val description: String,
    val graphical: Boolean,
    val icon: Drawable,
) {
    /**
     * Constants for source types
     */
    object Source {
        /**
         * Heart rate: includes a single required numeric field [Field.HEART_RATE]
         */
        const val HEART_RATE = "TYPE_HEART_RATE_ID"

        /**
         * Power: includes a single required numeric field [Field.POWER]
         */
        const val POWER = "TYPE_POWER_ID"

        /**
         * Raw cadence: includes a single required numeric field [Field.CADENCE]
         */
        const val CADENCE = "TYPE_CAD_CADENCE_ID"

        /**
         * Raw speed: includes a single required numeric field [Field.SPEED]
         */
        const val SPEED = "TYPE_SPD_SPEED_ID"

        /**
         * Radar: includes required field [Field.RADAR_THREAT_LEVEL] and optional fields [Field.RADAR_TARGET_1_RANGE], [Field.RADAR_TARGET_2_RANGE], [Field.RADAR_TARGET_3_RANGE], [Field.RADAR_TARGET_4_RANGE]
         */
        const val RADAR = "TYPE_RADAR_ID"

        /**
         * Shifting battery: includes optional fields [Field.SHIFTING_BATTERY_STATUS] [Field.SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR] [Field.SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR]
         */
        const val SHIFTING_BATTERY = "TYPE_SHIFTING_BATTERY_ID"

        /**
         * Shifting front gear: includes required field [Field.SHIFTING_FRONT_GEAR] and optional fields [Field.SHIFTING_FRONT_GEAR_TEETH] [Field.SHIFTING_FRONT_GEAR_MAX]
         */
        const val SHIFTING_FRONT_GEAR = "TYPE_SHIFTING_FRONT_GEAR_ID"

        /**
         * Shifting rear gear: includes required field [Field.SHIFTING_REAR_GEAR] and optional fields [Field.SHIFTING_REAR_GEAR_TEETH] [Field.SHIFTING_REAR_GEAR_MAX]
         */
        const val SHIFTING_REAR_GEAR = "TYPE_SHIFTING_REAR_GEAR_ID"
    }

    /**
     * Type constants generated from pre-existing data types used by Karoo.
     */
    object Type {
        /**
         * Category: Speed
         */

        /**
         * Speed - Current speed
         * Fields: [Field.SPEED]
         */
        const val SPEED = "TYPE_SPEED_ID"

        /**
         * Average Speed - Avg. speed this ride
         * Fields: [Field.AVERAGE_SPEED]
         */
        const val AVERAGE_SPEED = "TYPE_AVERAGE_SPEED_ID"

        /**
         * Max Speed - Max. speed this ride
         * Fields: [Field.MAX_SPEED]
         */
        const val MAX_SPEED = "TYPE_MAX_SPEED_ID"

        /**
         * 3s Average Speed - 3-second rolling avg.
         * Fields: [Field.SMOOTHED_3S_AVERAGE_SPEED]
         */
        const val SMOOTHED_3S_AVERAGE_SPEED = "TYPE_3S_AVERAGE_SPEED_ID"

        /**
         * 5s Average Speed - 5-second rolling avg.
         * Fields: [Field.SMOOTHED_5S_AVERAGE_SPEED]
         */
        const val SMOOTHED_5S_AVERAGE_SPEED = "TYPE_5S_AVERAGE_SPEED_ID"

        /**
         * 10s Average Speed - 10-second rolling avg.
         * Fields: [Field.SMOOTHED_10S_AVERAGE_SPEED]
         */
        const val SMOOTHED_10S_AVERAGE_SPEED = "TYPE_10S_AVERAGE_SPEED_ID"

        /**
         * Category: Distance
         */

        /**
         * Distance - Distance traveled this ride
         * Fields: [Field.DISTANCE]
         */
        const val DISTANCE = "TYPE_DISTANCE_ID"

        /**
         * Category: Heart Rate
         */

        /**
         * Heart Rate - Current HR
         * Fields: [Field.HEART_RATE]
         */
        const val HEART_RATE = "TYPE_HEART_RATE_ID"

        /**
         * Heart Rate Zone - Current HR zone
         * Fields: [Field.HR_ZONE]
         */
        const val HR_ZONE = "TYPE_HR_ZONE_ID"

        /**
         * Average Heart Rate - Avg. HR this ride
         * Fields: [Field.AVG_HR]
         */
        const val AVERAGE_HR = "TYPE_AVERAGE_HR_ID"

        /**
         * Max Heart Rate - Max. HR this ride
         * Fields: [Field.MAX_HR]
         */
        const val MAX_HR = "TYPE_MAX_HR_ID"

        /**
         * Percentage of Max HR - Ratio of current HR to your max.
         * Fields: [Field.PERCENT_MAX_HR]
         */
        const val PERCENT_MAX_HR = "TYPE_PERCENT_MAX_HR_ID"

        /**
         * Percentage of HRR - Ratio of current HR to your HR Reserve
         * Fields: [Field.PERCENT_HRR]
         */
        const val PERCENT_HRR = "TYPE_PERCENT_HRR_ID"

        /**
         * Average % of HRR - Avg. ratio of HR to your HR Reserve this ride
         * Fields: [Field.AVERAGE_PERCENT_HRR]
         */
        const val AVERAGE_PERCENT_HRR = "TYPE_AVERAGE_PERCENT_HRR_ID"

        /**
         * Category: Cadence
         */

        /**
         * Cadence - Current pedaling rate
         * Fields: [Field.CADENCE]
         */
        const val CADENCE = "TYPE_CADENCE_ID"

        /**
         * Average Cadence - Avg. pedaling rate this ride
         * Fields: [Field.AVERAGE_CADENCE]
         */
        const val AVERAGE_CADENCE = "TYPE_AVERAGE_CADENCE_ID"

        /**
         * Max Cadence - Max. pedaling rate this ride
         * Fields: [Field.MAX_CADENCE]
         */
        const val MAX_CADENCE = "TYPE_MAX_CADENCE_ID"

        /**
         * 3s Average Cadence - 3-second rolling avg.
         * Fields: [Field.SMOOTHED_3S_AVERAGE_CADENCE]
         */
        const val SMOOTHED_3S_AVERAGE_CADENCE = "TYPE_3S_AVERAGE_CADENCE_ID"

        /**
         * 5s Average Cadence - 5-second rolling avg.
         * Fields: [Field.SMOOTHED_5S_AVERAGE_CADENCE]
         */
        const val SMOOTHED_5S_AVERAGE_CADENCE = "TYPE_5S_AVERAGE_CADENCE_ID"

        /**
         * 10s Average Cadence - 10-second rolling avg.
         * Fields: [Field.SMOOTHED_10S_AVERAGE_CADENCE]
         */
        const val SMOOTHED_10S_AVERAGE_CADENCE = "TYPE_10S_AVERAGE_CADENCE_ID"

        /**
         * Category: Power
         */

        /**
         * Power - Current power output
         * Fields: [Field.POWER]
         */
        const val POWER = "TYPE_POWER_ID"

        /**
         * Power Zone - Current power zone
         * Fields: [Field.POWER_ZONE]
         */
        const val POWER_ZONE = "TYPE_POWER_ZONE_ID"

        /**
         * Average Power - Avg. power output this ride
         * Fields: [Field.AVERAGE_POWER]
         */
        const val AVERAGE_POWER = "TYPE_AVERAGE_POWER_ID"

        /**
         * Max Power - Max. power output this ride
         * Fields: [Field.MAX_POWER]
         */
        const val MAX_POWER = "TYPE_MAX_POWER_ID"

        /**
         * 3s Average Power - 3-second rolling avg.
         * Fields: [Field.SMOOTHED_3S_AVERAGE_POWER]
         */
        const val SMOOTHED_3S_AVERAGE_POWER = "TYPE_3S_AVERAGE_POWER_ID"

        /**
         * 5s Average Power - 5-second rolling avg.
         * Fields: [Field.SMOOTHED_5S_AVERAGE_POWER]
         */
        const val SMOOTHED_5S_AVERAGE_POWER = "TYPE_5S_AVERAGE_POWER_ID"

        /**
         * 10s Average Power - 10-second rolling avg.
         * Fields: [Field.SMOOTHED_10S_AVERAGE_POWER]
         */
        const val SMOOTHED_10S_AVERAGE_POWER = "TYPE_10S_AVERAGE_POWER_ID"

        /**
         * 30s Average Power - 30-second rolling avg.
         * Fields: [Field.SMOOTHED_30S_AVERAGE_POWER]
         */
        const val SMOOTHED_30S_AVERAGE_POWER = "TYPE_30S_AVERAGE_POWER_ID"

        /**
         * Normalized Power® - Normalized Power® this ride
         * Fields: [Field.NORMALIZED_POWER]
         */
        const val NORMALIZED_POWER = "TYPE_NORMALIZED_POWER_ID"

        /**
         * Percentage of FTP - Current power as a % of functional threshold power
         * Fields: [Field.PERCENT_MAX_FTP]
         */
        const val PERCENT_MAX_FTP = "TYPE_PERCENT_MAX_FTP_ID"

        /**
         * Intensity Factor® - Intensity Factor® this ride
         * Fields: [Field.INTENSITY_FACTOR]
         */
        const val INTENSITY_FACTOR = "TYPE_INTENSITY_FACTOR_ID"

        /**
         * Training Stress Score® - Training Stress Score® this ride
         * Fields: [Field.TRAINING_STRESS_SCORE]
         */
        const val TRAINING_STRESS_SCORE = "TYPE_TRAINING_STRESS_SCORE_ID"

        /**
         * 20min Average Power - 20-min. rolling avg.
         * Fields: [Field.SMOOTHED_20M_AVERAGE_POWER]
         */
        const val SMOOTHED_20M_AVERAGE_POWER = "TYPE_20M_AVERAGE_POWER_ID"

        /**
         * 1hr Average Power - 1-hr. rolling avg.
         * Fields: [Field.SMOOTHED_1HR_AVERAGE_POWER]
         */
        const val SMOOTHED_1HR_AVERAGE_POWER = "TYPE_1HR_AVERAGE_POWER_ID"

        /**
         * Power Balance - Current left/right power balance
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT], [Field.POWER]
         */
        const val PEDAL_POWER_BALANCE = "TYPE_PEDAL_POWER_BALANCE_ID"

        /**
         * Average Power Balance - Avg. left/right power balance this ride
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val AVERAGE_PEDAL_POWER_BALANCE = "TYPE_AVERAGE_PEDAL_POWER_BALANCE_ID"

        /**
         * 3s Average Power Balance - 3-second rolling avg.
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val SMOOTHED_3S_AVERAGE_PEDAL_POWER_BALANCE = "TYPE_3S_AVERAGE_PEDAL_POWER_BALANCE_ID"

        /**
         * 5s Average Power Balance - 5-second rolling avg.
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val SMOOTHED_5S_AVERAGE_PEDAL_POWER_BALANCE = "TYPE_5S_AVERAGE_PEDAL_POWER_BALANCE_ID"

        /**
         * 10s Average Power Balance - 10-second rolling avg.
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val SMOOTHED_10S_AVERAGE_PEDAL_POWER_BALANCE = "TYPE_10S_AVERAGE_PEDAL_POWER_BALANCE_ID"

        /**
         * 30s Average Power Balance - 30-second rolling avg.
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val SMOOTHED_30S_AVERAGE_PEDAL_POWER_BALANCE = "TYPE_30S_AVERAGE_PEDAL_POWER_BALANCE_ID"

        /**
         * Pedal Smoothness - Measure of pedal stroke steadiness
         * Fields: [Field.PEDAL_SMOOTHNESS_LEFT], [Field.PEDAL_SMOOTHNESS_RIGHT], [Field.POWER]
         */
        const val PEDAL_SMOOTHNESS = "TYPE_PEDAL_SMOOTHNESS_ID"

        /**
         * Torque Effectiveness - Measure of pedal stroke efficiency
         * Fields: [Field.TORQUE_EFFECTIVENESS_LEFT], [Field.TORQUE_EFFECTIVENESS_RIGHT], [Field.POWER]
         */
        const val TORQUE_EFFECTIVENESS = "TYPE_TORQUE_EFFECTIVENESS_ID"

        /**
         * Power to Weight - Power output in watts per kilogram
         * Fields: [Field.POWER_TO_WEIGHT]
         */
        const val POWER_TO_WEIGHT = "TYPE_POWER_TO_WEIGHT"

        /**
         * Energy Output - Amount of energy used this ride
         * Fields: [Field.ENERGY_OUTPUT]
         */
        const val ENERGY_OUTPUT = "TYPE_ENERGY_OUTPUT_ID"

        /**
         * Calories from Power - Calories burned this ride
         * Fields: [Field.CALORIES]
         */
        const val CALORIES = "TYPE_CALORIES_ID"

        /**
         * Calorie Burn Rate - Calories burned per hour this ride
         * Fields: [Field.CALORIES_PER_HOUR]
         */
        const val CALORIES_PER_HOUR = "TYPE_CALORIES_PER_HOUR_ID"

        /**
         * Variability Index - Measure of power steadiness this ride
         * Fields: [Field.VARIABILITY_INDEX]
         */
        const val VARIABILITY_INDEX = "TYPE_VARIABILITY_INDEX_ID"

        /**
         * Torque - Current torque in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val TORQUE = "TYPE_TORQUE_ID"

        /**
         * 3s Average Torque - 3-second rolling avg. in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val SMOOTHED_3S_AVERAGE_TORQUE = "TYPE_3S_AVERAGE_TORQUE_ID"

        /**
         * Average Torque - Average torque this ride in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val AVERAGE_TORQUE = "TYPE_AVERAGE_TORQUE_ID"

        /**
         * Max Torque - Max. torque this ride in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val MAX_TORQUE = "TYPE_MAX_TORQUE_ID"

        /**
         * Category: Lap
         */

        /**
         * Lap Number - # of current lap
         * Fields: [Field.LAP_NUMBER]
         */
        const val LAP_NUMBER = "TYPE_LAP_NUMBER_ID"

        /**
         * Lap Time - Recording duration this lap
         * Fields: [Field.ELAPSED_TIME]
         */
        const val ELAPSED_TIME_LAP = "TYPE_ELAPSED_TIME_LAP_ID"

        /**
         * Lap Distance - Distance traveled this lap
         * Fields: [Field.DISTANCE]
         */
        const val DISTANCE_LAP = "TYPE_DISTANCE_LAP_ID"

        /**
         * Lap Speed - Avg. speed this lap
         * Fields: [Field.AVERAGE_SPEED]
         */
        const val AVERAGE_SPEED_LAP = "TYPE_AVERAGE_SPEED_LAP_ID"

        /**
         * Lap Max Speed - Max. speed this lap
         * Fields: [Field.MAX_SPEED]
         */
        const val MAX_SPEED_LAP = "TYPE_MAX_SPEED_LAP_ID"

        /**
         * Lap Heart Rate - Avg. heart rate this lap
         * Fields: [Field.AVG_HR]
         */
        const val AVERAGE_LAP_HR = "TYPE_AVERAGE_LAP_HR_ID"

        /**
         * Lap Max Heart Rate - Max. heart rate this lap
         * Fields: [Field.MAX_HR]
         */
        const val MAX_HR_LAP = "TYPE_MAX_HR_LAP_ID"

        /**
         * Lap Cadence - Avg. cadence this lap
         * Fields: [Field.AVERAGE_CADENCE]
         */
        const val CADENCE_LAP = "TYPE_CADENCE_LAP_ID"

        /**
         * Lap Max Cadence - Max. cadence this lap
         * Fields: [Field.MAX_CADENCE]
         */
        const val MAX_CADENCE_LAP = "TYPE_MAX_CADENCE_LAP_ID"

        /**
         * Lap Power - Avg. power output this lap
         * Fields: [Field.AVERAGE_POWER]
         */
        const val POWER_LAP = "TYPE_POWER_LAP_ID"

        /**
         * Lap Normalized Power® - Normalized Power® this lap
         * Fields: [Field.NORMALIZED_POWER]
         */
        const val NORMALIZED_POWER_LAP = "TYPE_NORMALIZED_POWER_LAP_ID"

        /**
         * Lap Power Balance - Avg. left/right power balance this lap
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val AVERAGE_PEDAL_POWER_BALANCE_LAP = "TYPE_AVERAGE_PEDAL_POWER_BALANCE_LAP_ID"

        /**
         * Lap Max Power - Max. power output this lap
         * Fields: [Field.MAX_POWER]
         */
        const val MAX_POWER_LAP = "TYPE_MAX_POWER_LAP_ID"

        /**
         * Lap Torque - Avg. torque this lap in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val TORQUE_LAP = "TYPE_TORQUE_LAP_ID"

        /**
         * Lap Max Torque - Max. torque this lap in N⋅m
         * Fields: [Field.TORQUE]
         */
        const val MAX_TORQUE_LAP = "TYPE_MAX_TORQUE_LAP_ID"

        /**
         * Lap VAM - Avg. VAM this lap in meters/hour
         * Fields: [Field.VERTICAL_SPEED]
         */
        const val AVERAGE_VERTICAL_SPEED_LAP = "TYPE_AVERAGE_VERTICAL_SPEED_LAP_ID"

        /**
         * Lap Ascent - Total elevation gain this lap
         * Fields: [Field.ELEVATION_GAIN]
         */
        const val ELEVATION_GAIN_LAP = "TYPE_ELEVATION_GAIN_LAP_ID"

        /**
         * Lap Descent - Total elevation descended this lap
         * Fields: [Field.ELEVATION_LOSS]
         */
        const val ELEVATION_LOSS_LAP = "TYPE_ELEVATION_LOSS_LAP_ID"

        /**
         * Lap Min Elevation - Min. altitude this lap
         * Fields: [Field.MIN_ELEVATION]
         */
        const val MIN_ELEVATION_LAP = "TYPE_MIN_ELEVATION_LAP_ID"

        /**
         * Lap Max Elevation - Max. altitude this lap
         * Fields: [Field.MAX_ELEVATION]
         */
        const val MAX_ELEVATION_LAP = "TYPE_MAX_ELEVATION_LAP_ID"

        /**
         * Lap Elevation - Avg. altitude this lap
         * Fields: [Field.AVERAGE_ELEVATION]
         */
        const val AVERAGE_ELEVATION_LAP = "TYPE_AVERAGE_ELEVATION_LAP_ID"

        /**
         * Lap Core Temp - Avg. core body temperature this lap
         * Fields: [Field.CORE_TEMP]
         */
        const val AVERAGE_CORE_TEMP_LAP = "TYPE_AVERAGE_CORE_TEMP_LAP_ID"

        /**
         * Lap Max Core Temp - Max. core body temperature this lap
         * Fields: [Field.CORE_TEMP]
         */
        const val MAX_CORE_TEMP_LAP = "TYPE_MAX_CORE_TEMP_LAP_ID"

        /**
         * Lap Skin Temp - Avg. skin temperature this lap
         * Fields: [Field.SKIN_TEMP]
         */
        const val AVERAGE_SKIN_TEMP_LAP = "TYPE_AVERAGE_SKIN_TEMP_LAP_ID"

        /**
         * Lap Max Skin Temp - Max. skin temperature this lap
         * Fields: [Field.SKIN_TEMP]
         */
        const val MAX_SKIN_TEMP_LAP = "TYPE_MAX_SKIN_TEMP_LAP_ID"

        /**
         * Category: Last Lap
         */

        /**
         * Last Lap Time - Recording duration of previous lap
         * Fields: [Field.ELAPSED_TIME]
         */
        const val ELAPSED_TIME_LAST_LAP = "TYPE_ELAPSED_TIME_LAST_LAP_ID"

        /**
         * Last Lap Distance - Distance traveled in previous lap
         * Fields: [Field.DISTANCE]
         */
        const val DISTANCE_LAP_LAST_LAP = "TYPE_DISTANCE_LAP_LAST_LAP_ID"

        /**
         * Last Lap Speed - Avg. speed of previous lap
         * Fields: [Field.AVERAGE_SPEED]
         */
        const val AVERAGE_SPEED_LAST_LAP = "TYPE_AVERAGE_SPEED_LAST_LAP_ID"

        /**
         * Last Lap Heart Rate - Avg. heart rate of previous lap
         * Fields: [Field.AVG_HR]
         */
        const val AVERAGE_HR_LAST_LAP = "TYPE_AVERAGE_HR_LAST_LAP_ID"

        /**
         * Last Lap Cadence - Avg. cadence of previous lap
         * Fields: [Field.AVERAGE_CADENCE]
         */
        const val AVERAGE_CADENCE_LAST_LAP = "TYPE_AVERAGE_CADENCE_LAST_LAP_ID"

        /**
         * Last Lap Power - Avg. power output in previous lap
         * Fields: [Field.AVERAGE_POWER]
         */
        const val AVERAGE_POWER_LAST_LAP = "TYPE_AVERAGE_POWER_LAST_LAP_ID"

        /**
         * Last Lap Normalized Power® - Normalized Power® of previous lap
         * Fields: [Field.NORMALIZED_POWER]
         */
        const val NORMALIZED_POWER_LAST_LAP = "TYPE_NORMALIZED_POWER_LAST_LAP_ID"

        /**
         * Last Lap Power Balance - Avg. left/right power balance of previous lap
         * Fields: [Field.PEDAL_POWER_BALANCE_LEFT]
         */
        const val AVERAGE_PEDAL_POWER_BALANCE_LAST_LAP = "TYPE_AVERAGE_PEDAL_POWER_BALANCE_LAST_LAP_ID"

        /**
         * Last Lap Ascent - Total elevation gain of previous lap
         * Fields: [Field.ELEVATION_GAIN]
         */
        const val ELEVATION_GAIN_LAST_LAP = "TYPE_ELEVATION_GAIN_LAST_LAP_ID"

        /**
         * Last Lap Descent - Total elevation descended in previous lap
         * Fields: [Field.ELEVATION_LOSS]
         */
        const val ELEVATION_LOSS_LAST_LAP = "TYPE_ELEVATION_LOSS_LAST_LAP_ID"

        /**
         * Category: Climb
         */

        /**
         * Current Elevation - Current altitude above or below sea level
         * Fields: [Field.PRESSURE_ELEVATION]
         */
        const val PRESSURE_ELEVATION_CORRECTION = "TYPE_PRESSURE_ELEVATION_CORRECTION_ID"

        /**
         * Ascent - Total elevation gain this ride
         * Fields: [Field.ELEVATION_GAIN]
         */
        const val ELEVATION_GAIN = "TYPE_ELEVATION_GAIN_ID"

        /**
         * Descent - Total elevation descended this ride
         * Fields: [Field.ELEVATION_LOSS]
         */
        const val ELEVATION_LOSS = "TYPE_ELEVATION_LOSS_ID"

        /**
         * Current Grade % - Steepness of current surface
         * Fields: [Field.ELEVATION_GRADE]
         */
        const val ELEVATION_GRADE = "TYPE_ELEVATION_GRADE_ID"

        /**
         * VAM - Current rate of ascent in meters/hour
         * Fields: [Field.VERTICAL_SPEED]
         */
        const val VERTICAL_SPEED = "TYPE_VERTICAL_SPEED_ID"

        /**
         * Average VAM - Avg. VAM this ride in meters/hour
         * Fields: [Field.VERTICAL_SPEED]
         */
        const val AVERAGE_VERTICAL_SPEED = "TYPE_AVERAGE_VERTICAL_SPEED_ID"

        /**
         * 30s Average VAM - 30-second rolling avg. in meters/hour
         * Fields: [Field.VERTICAL_SPEED]
         */
        const val AVERAGE_VERTICAL_SPEED_30S = "TYPE_AVERAGE_VERTICAL_SPEED_30S_ID"

        /**
         * Min Elevation - Lowest altitude this ride
         * Fields: [Field.MIN_ELEVATION]
         */
        const val MIN_ELEVATION = "TYPE_MIN_ELEVATION_ID"

        /**
         * Max Elevation - Highest altitude this ride
         * Fields: [Field.MAX_ELEVATION]
         */
        const val MAX_ELEVATION = "TYPE_MAX_ELEVATION_ID"

        /**
         * Avg Elevation - Mean elevation this ride
         * Fields: [Field.AVERAGE_ELEVATION]
         */
        const val AVERAGE_ELEVATION = "TYPE_AVERAGE_ELEVATION_ID"

        /**
         * Distance to Top - Distance left to top of current climb
         * Fields: [Field.DISTANCE_TO_TOP]
         */
        const val DISTANCE_TO_TOP = "TYPE_DISTANCE_TO_TOP_ID"

        /**
         * Elevation to Top - Elevation left to top of current climb
         * Fields: [Field.ELEVATION_TO_TOP]
         */
        const val ELEVATION_TO_TOP = "TYPE_ELEVATION_TO_TOP_ID"

        /**
         * Distance from Base - Distance since the start of current climb
         * Fields: [Field.DISTANCE_FROM_BOTTOM]
         */
        const val DISTANCE_FROM_BOTTOM = "TYPE_DISTANCE_FROM_BOTTOM_ID"

        /**
         * Elevation from Base - Elevation above the start of current climb
         * Fields: [Field.ELEVATION_FROM_BOTTOM]
         */
        const val ELEVATION_FROM_BOTTOM = "TYPE_ELEVATION_FROM_BOTTOM_ID"

        /**
         * Category: Time
         */

        /**
         * Total Time - Time since this ride began, including paused time
         * Fields: [Field.RIDE_TIME]
         */
        const val RIDE_TIME = "TYPE_RIDE_TIME_ID"

        /**
         * Time of Day - Time of day at current location
         * Fields: [Field.CLOCK_TIME]
         */
        const val CLOCK_TIME = "TYPE_CLOCK_TIME_ID"

        /**
         * Ride Time - Time spent recording this ride
         * Fields: [Field.ELAPSED_TIME]
         */
        const val ELAPSED_TIME = "TYPE_ELAPSED_TIME_ID"

        /**
         * Paused Time - Time spent paused this ride
         * Fields: [Field.PAUSED_TIME]
         */
        const val PAUSED_TIME = "TYPE_PAUSED_TIME_ID"

        /**
         * Sunrise - Time of Sunrise at current location
         * Fields: [Field.SUNRISE]
         */
        const val SUNRISE = "TYPE_SUNRISE_ID"

        /**
         * Sunset - Time of Sunset at current location
         * Fields: [Field.SUNSET]
         */
        const val SUNSET = "TYPE_SUNSET_ID"

        /**
         * Civil Dawn - Time of Civil Dawn at current location
         * Fields: [Field.CIVIL_DAWN]
         */
        const val CIVIL_DAWN = "TYPE_CIVIL_DAWN_ID"

        /**
         * Civil Dusk - Time of Civil Dusk at current location
         * Fields: [Field.CIVIL_DUSK]
         */
        const val CIVIL_DUSK = "TYPE_CIVIL_DUSK_ID"

        /**
         * Time to Sunrise - Time till Sunrise at current location
         * Fields: [Field.TIME_TO_SUNRISE]
         */
        const val TIME_TO_SUNRISE = "TYPE_TIME_TO_SUNRISE_ID"

        /**
         * Time to Sunset - Time till Sunset at current location
         * Fields: [Field.TIME_TO_SUNSET]
         */
        const val TIME_TO_SUNSET = "TYPE_TIME_TO_SUNSET_ID"

        /**
         * Time to Civil Dawn - Time till Civil Dawn at current location
         * Fields: [Field.TIME_TO_CIVIL_DAWN]
         */
        const val TIME_TO_CIVIL_DAWN = "TYPE_TIME_TO_CIVIL_DAWN_ID"

        /**
         * Time to Civil Dusk - Time till Civil Dusk at current location
         * Fields: [Field.TIME_TO_CIVIL_DUSK]
         */
        const val TIME_TO_CIVIL_DUSK = "TYPE_TIME_TO_CIVIL_DUSK_ID"

        /**
         * Category: Shifting
         */

        /**
         * Gears - Current front and rear gear
         * Fields: [Field.SHIFTING_FRONT_GEAR], [Field.SHIFTING_FRONT_GEAR_TEETH], [Field.SHIFTING_FRONT_GEAR_MAX], [Field.SHIFTING_REAR_GEAR], [Field.SHIFTING_REAR_GEAR_TEETH], [Field.SHIFTING_REAR_GEAR_MAX]
         */
        const val SHIFTING_GEARS = "TYPE_SHIFTING_GEARS_ID"

        /**
         * Front Gear - Chainring being used currently
         * Fields: [Field.SHIFTING_FRONT_GEAR], [Field.SHIFTING_FRONT_GEAR_TEETH], [Field.SHIFTING_FRONT_GEAR_MAX]
         */
        const val SHIFTING_FRONT_GEAR = "TYPE_SHIFTING_FRONT_GEAR_ID"

        /**
         * Rear Gear - Cassette sprocket being used currently
         * Fields: [Field.SHIFTING_REAR_GEAR], [Field.SHIFTING_REAR_GEAR_TEETH], [Field.SHIFTING_REAR_GEAR_MAX]
         */
        const val SHIFTING_REAR_GEAR = "TYPE_SHIFTING_REAR_GEAR_ID"

        /**
         * Drivetrain Battery - Drivetrain battery level
         * Fields: [Field.SHIFTING_BATTERY_STATUS], [Field.SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR], [Field.SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR]
         */
        const val SHIFTING_BATTERY = "TYPE_SHIFTING_BATTERY_ID"

        /**
         * Total Shifts - Total number of shifts this ride (front + rear)
         * Fields: [Field.SHIFTING_COUNT]
         */
        const val SHIFTING_COUNT = "TYPE_SHIFTING_COUNT_ID"

        /**
         * Front Shifts - # of front chainring shifts in ride
         * Fields: [Field.SHIFTING_COUNT]
         */
        const val SHIFTING_COUNT_FRONT = "TYPE_SHIFTING_COUNT_FRONT_ID"

        /**
         * Rear Shifts - # of rear derailleur shifts in ride
         * Fields: [Field.SHIFTING_COUNT]
         */
        const val SHIFTING_COUNT_REAR = "TYPE_SHIFTING_COUNT_REAR_ID"

        /**
         * Category: Miscellaneous
         */

        /**
         * Battery - Karoo battery level
         * Fields: [Field.BATTERY_PERCENT]
         */
        const val BATTERY_PERCENT = "TYPE_BATTERY_PERCENT_ID"

        /**
         * Front Pressure - Tire pressure in front tire
         * Fields: [Field.TIRE_PRESSURE], [Field.TIRE_PRESSURE_TARGET], [Field.TIRE_PRESSURE_RANGE]
         */
        const val TIRE_PRESSURE_FRONT = "TYPE_TIRE_PRESSURE_FRONT_ID"

        /**
         * Rear Pressure - Tire pressure in rear tire
         * Fields: [Field.TIRE_PRESSURE], [Field.TIRE_PRESSURE_TARGET], [Field.TIRE_PRESSURE_RANGE]
         */
        const val TIRE_PRESSURE_REAR = "TYPE_TIRE_PRESSURE_REAR_ID"

        /**
         * Temperature - Current air temperature
         * Fields: [Field.TEMPERATURE]
         */
        const val TEMPERATURE = "TYPE_TEMPERATURE_ID"

        /**
         * Location
         * Fields: [Field.LOC_LATITUDE], [Field.LOC_LONGITUDE], [Field.LOC_BEARING], [Field.LOC_ACCURACY]
         */
        const val LOCATION = "TYPE_LOCATION_ID"

        /**
         * Current radar state
         * Fields: [Field.RADAR_THREAT_LEVEL], [Field.RADAR_TARGET_1_RANGE], [Field.RADAR_TARGET_2_RANGE], [Field.RADAR_TARGET_3_RANGE], [Field.RADAR_TARGET_4_RANGE], [Field.RADAR_TARGET_5_RANGE], [Field.RADAR_TARGET_6_RANGE], [Field.RADAR_TARGET_7_RANGE], [Field.RADAR_TARGET_8_RANGE]
         */
        const val RADAR = "TYPE_RADAR_ID"

        /**
         * Category: Navigation
         */

        /**
         * Distance to Destination - Distance to end of route
         * Fields: [Field.DISTANCE_TO_DESTINATION], [Field.NAVIGATION_STATE], [Field.REROUTING_ENABLED], [Field.ON_ROUTE]
         */
        const val DISTANCE_TO_DESTINATION = "TYPE_DISTANCE_TO_DESTINATION_ID"

        /**
         * Distance to Next Turn - Distance till next navigation turn
         * Fields: [Field.DISTANCE_TO_NEXT_TURN]
         */
        const val DISTANCE_TO_NEXT_TURN = "TYPE_DISTANCE_TO_NEXT_TURN_ID"

        /**
         * Elevation Remaining - Total remaining ascent on current route
         * Fields: [Field.ELEVATION_REMAINING], [Field.NAVIGATION_STATE], [Field.REROUTING_ENABLED], [Field.ON_ROUTE]
         */
        const val ELEVATION_REMAINING = "TYPE_ELEVATION_REMAINING_ID"

        /**
         * Time to Destination - Estimated time to reach navigation destination
         * Fields: [Field.TIME_TO_DESTINATION]
         */
        const val TIME_TO_DESTINATION = "TYPE_TIME_TO_DESTINATION_ID"

        /**
         * Time of Arrival - Estimated time of arrival to navigation destination
         * Fields: [Field.TIME_OF_ARRIVAL]
         */
        const val TIME_OF_ARRIVAL = "TYPE_TIME_OF_ARRIVAL_ID"

        /**
         * Heading - Current compass direction of travel
         * Fields: [Field.HEADING]
         */
        const val HEADING = "TYPE_HEADING_ID"

        /**
         * Category: eBike
         */

        /**
         * Bike Battery - eBike battery level
         * Fields: [Field.LEV_BATTERY_STATUS]
         */
        const val LEV_BATTERY_STATUS = "TYPE_LEV_BATTERY_STATUS_ID"

        /**
         * Est Range Remaining - Estimated eBike range in current assist mode
         * Fields: [Field.LEV_ESTIMATED_RANGE]
         */
        const val LEV_ESTIMATED_RANGE = "TYPE_LEV_ESTIMATED_RANGE_ID"

        /**
         * Assist Mode/Level - Current bike assist mode/level
         * Fields: [Field.LEV_ASSIST_MODE], [Field.LEV_SUPPORTED_ASSIST_MODES]
         */
        const val LEV_ASSIST_MODE = "TYPE_LEV_ASSIST_MODE_ID"

        /**
         * Burn Rate - Rate of energy consumption, in wh/km or wh/mi
         * Fields: [Field.LEV_ENERGY_CONSUMPTION]
         */
        const val LEV_ENERGY_CONSUMPTION = "TYPE_LEV_ENERGY_CONSUMPTION_ID"

        /**
         * 20M Burn Rate - Average rate of energy consumption over the last 20 min
         * Fields: [Field.LEV_20MIN_ENERGY_CONSUMPTION]
         */
        const val LEV_20M_ENERGY_CONSUMPTION = "TYPE_LEV_20M_ENERGY_CONSUMPTION_ID"

        /**
         * Bike Motor Power - Current bike motor power output in watts
         * Fields: [Field.LEV_MOTOR_POWER]
         */
        const val LEV_MOTOR_POWER = "TYPE_LEV_MOTOR_POWER_ID"

        /**
         * Combined Power - Current combined rider and motor power in watts
         * Fields: [Field.LEV_COMBINED_POWER]
         */
        const val LEV_COMBINED_POWER = "TYPE_LEV_COMBINED_POWER_ID"

        /**
         * Category: Body Temp
         */

        /**
         * Core Body Temp - Current core body temperature
         * Fields: [Field.CORE_TEMP], [Field.CORE_DATA_QUALITY], [Field.CORE_RESERVED]
         */
        const val CORE_TEMP = "TYPE_CORE_TEMP_ID"

        /**
         * Average Core Body Temp - Avg. core body temperature this ride
         * Fields: [Field.CORE_TEMP]
         */
        const val AVERAGE_CORE_TEMP = "TYPE_AVERAGE_CORE_TEMP_ID"

        /**
         * Max Core Body Temp - Max. core body temperature this ride
         * Fields: [Field.CORE_TEMP]
         */
        const val MAX_CORE_TEMP = "TYPE_MAX_CORE_TEMP_ID"

        /**
         * Skin Temp - Current skin temperature
         * Fields: [Field.SKIN_TEMP], [Field.CORE_DATA_QUALITY]
         */
        const val SKIN_TEMP = "TYPE_SKIN_TEMP_ID"

        /**
         * Average Skin Temp - Avg. skin temperature this ride
         * Fields: [Field.SKIN_TEMP]
         */
        const val AVERAGE_SKIN_TEMP = "TYPE_AVERAGE_SKIN_TEMP_ID"

        /**
         * Max Skin Temp - Max. skin temperature this ride
         * Fields: [Field.SKIN_TEMP]
         */
        const val MAX_SKIN_TEMP = "TYPE_MAX_SKIN_TEMP_ID"
    }

    /**
     * Field constants generated from pre-existing fields used by Karoo.
     */
    object Field {
        const val LOC_LATITUDE = "FIELD_LOC_LATITUDE_ID"
        const val LOC_LONGITUDE = "FIELD_LOC_LONGITUDE_ID"

        /**
         * Optional
         */
        const val LOC_ACCURACY = "FIELD_LOC_ACCURACY_ID"

        /**
         * Optional
         */
        const val LOC_BEARING = "FIELD_LOC_BEARING_ID"
        const val SPEED = "FIELD_SPEED_ID"
        const val HEART_RATE = "FIELD_HEART_RATE_ID"
        const val CADENCE = "FIELD_CADENCE_ID"
        const val AVERAGE_CADENCE = "FIELD_AVERAGE_CADENCE_ID"
        const val MAX_CADENCE = "FIELD_MAX_CADENCE_ID"
        const val DISTANCE = "FIELD_DISTANCE_ID"
        const val AVERAGE_SPEED = "FIELD_AVERAGE_SPEED_ID"
        const val ELAPSED_TIME = "FIELD_ELAPSED_TIME_ID"
        const val RIDE_TIME = "FIELD_RIDE_TIME_ID"
        const val POWER_TO_WEIGHT = "FIELD_POWER_TO_WEIGHT_ID"
        const val CLOCK_TIME = "FIELD_CLOCK_TIME_ID"
        const val POWER = "FIELD_POWER_ID"

        /**
         * Optional
         */
        const val PEDAL_POWER_BALANCE_LEFT = "FIELD_PEDAL_POWER_BALANCE_LEFT_ID"

        /**
         * Optional
         */
        const val PEDAL_SMOOTHNESS_LEFT = "FIELD_PEDAL_SMOOTHNESS_LEFT_ID"

        /**
         * Optional
         */
        const val PEDAL_SMOOTHNESS_RIGHT = "FIELD_PEDAL_SMOOTHNESS_RIGHT_ID"

        /**
         * Optional
         */
        const val TORQUE_EFFECTIVENESS_LEFT = "FIELD_TORQUE_EFFECTIVENESS_LEFT_ID"

        /**
         * Optional
         */
        const val TORQUE_EFFECTIVENESS_RIGHT = "FIELD_TORQUE_EFFECTIVENESS_RIGHT_ID"
        const val AVERAGE_POWER = "FIELD_AVERAGE_POWER_ID"
        const val SINGLE = "FIELD_SINGLE_ID"
        const val ENERGY_OUTPUT = "FIELD_ENERGY_OUTPUT_ID"
        const val CALORIES = "FIELD_CALORIES_ID"
        const val CALORIES_PER_HOUR = "FIELD_CALORIES_PER_HOUR_ID"
        const val TORQUE = "FIELD_TORQUE_ID"
        const val MAX_POWER = "FIELD_MAX_POWER_ID"
        const val MAX_SPEED = "FIELD_MAX_SPEED_ID"
        const val PERCENT_HRR = "FIELD_PERCENT_HRR_ID"
        const val AVERAGE_PERCENT_HRR = "FIELD_AVERAGE_PERCENT_HRR_ID"
        const val AVG_HR = "FIELD_AVG_HR_ID"
        const val MAX_HR = "FIELD_MAX_HR_ID"
        const val PRESSURE_ELEVATION = "FIELD_PRESSURE_ELEVATION_ID"
        const val ELEVATION_GAIN = "FIELD_ELEVATION_GAIN_ID"
        const val ELEVATION_LOSS = "FIELD_ELEVATION_LOSS_ID"

        /**
         * Optional
         */
        const val ELEVATION_REMAINING = "FIELD_ELEVATION_REMAINING_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         *   - 0: off route
         *   - 1: on route
         */
        const val ON_ROUTE = "FIELD_ON_ROUTE_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val REROUTING_ENABLED = "FIELD_REROUTING_ENABLED_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val NAVIGATION_STATE = "FIELD_NAVIGATION_STATE_ID"
        const val SMOOTHED_3S_AVERAGE_CADENCE = "FIELD_3S_AVERAGE_CADENCE_ID"
        const val SMOOTHED_5S_AVERAGE_CADENCE = "FIELD_5S_AVERAGE_CADENCE_ID"
        const val SMOOTHED_10S_AVERAGE_CADENCE = "FIELD_10S_AVERAGE_CADENCE_ID"
        const val SMOOTHED_3S_AVERAGE_POWER = "FIELD_3S_AVERAGE_POWER_ID"
        const val SMOOTHED_5S_AVERAGE_POWER = "FIELD_5S_AVERAGE_POWER_ID"
        const val SMOOTHED_10S_AVERAGE_POWER = "FIELD_10S_AVERAGE_POWER_ID"
        const val SMOOTHED_30S_AVERAGE_POWER = "FIELD_30S_AVERAGE_POWER_ID"
        const val SMOOTHED_20M_AVERAGE_POWER = "FIELD_20M_AVERAGE_POWER_ID"
        const val SMOOTHED_1HR_AVERAGE_POWER = "FIELD_1HR_AVERAGE_POWER_ID"
        const val SMOOTHED_3S_AVERAGE_SPEED = "FIELD_3S_AVERAGE_SPEED_ID"
        const val SMOOTHED_5S_AVERAGE_SPEED = "FIELD_5S_AVERAGE_SPEED_ID"
        const val SMOOTHED_10S_AVERAGE_SPEED = "FIELD_10S_AVERAGE_SPEED_ID"
        const val SMOOTHED_1HR_AVERAGE_SPEED = "FIELD_1HR_AVERAGE_SPEED_ID"

        /**
         * Optional
         */
        const val DISTANCE_TO_DESTINATION = "FIELD_DISTANCE_TO_DESTINATION_ID"

        /**
         * Optional
         */
        const val DISTANCE_TO_NEXT_TURN = "FIELD_DISTANCE_TO_NEXT_TURN_ID"
        const val DISTANCE_FROM_BOTTOM = "FIELD_DISTANCE_FROM_BOTTOM_ID"
        const val DISTANCE_TO_TOP = "FIELD_DISTANCE_TO_TOP_ID"
        const val ELEVATION_FROM_BOTTOM = "FIELD_ELEVATION_FROM_BOTTOM_ID"
        const val ELEVATION_TO_TOP = "FIELD_ELEVATION_TO_TOP_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val CLIMB_NUMBER = "FIELD_CLIMB_NUMBER_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val TOTAL_CLIMBS = "FIELD_TOTAL_CLIMBS_ID"
        const val TIME_TO_DESTINATION = "FIELD_TIME_TO_DESTINATION_ID"
        const val TIME_OF_ARRIVAL = "FIELD_TIME_OF_ARRIVAL_ID"
        const val SUNRISE = "FIELD_SUNRISE_ID"
        const val SUNSET = "FIELD_SUNSET_ID"
        const val CIVIL_DAWN = "FIELD_CIVIL_DAWN_ID"
        const val CIVIL_DUSK = "FIELD_CIVIL_DUSK_ID"
        const val TIME_TO_SUNRISE = "FIELD_TIME_TO_SUNRISE_ID"
        const val TIME_TO_SUNSET = "FIELD_TIME_TO_SUNSET_ID"
        const val TIME_TO_CIVIL_DAWN = "FIELD_TIME_TO_CIVIL_DAWN_ID"
        const val TIME_TO_CIVIL_DUSK = "FIELD_TIME_TO_CIVIL_DUSK_ID"
        const val HR_ZONE = "FIELD_HR_ZONE_ID"
        const val POWER_ZONE = "FIELD_POWER_ZONE_ID"
        const val VARIABILITY_INDEX = "FIELD_VARIABILITY_INDEX_ID"
        const val PAUSED_TIME = "FIELD_PAUSED_TIME_ID"

        /**
         * Optional
         */
        const val PERCENT_MAX_HR = "FIELD_PERCENT_MAX_HR_ID"

        /**
         * Optional
         */
        const val PERCENT_MAX_FTP = "FIELD_PERCENT_MAX_FTP_ID"
        const val TEMPERATURE = "FIELD_TEMPERATURE_ID"
        const val CORE_TEMP = "FIELD_CORE_TEMP_ID"
        const val SKIN_TEMP = "FIELD_SKIN_TEMP_ID"

        /**
         * Optional
         */
        const val ELEVATION_GRADE = "FIELD_ELEVATION_GRADE_ID"

        /**
         * Optional
         */
        const val VERTICAL_SPEED = "FIELD_VERTICAL_SPEED_ID"
        const val NORMALIZED_POWER = "FIELD_NORMALIZED_POWER_ID"
        const val TRAINING_STRESS_SCORE = "FIELD_TRAINING_STRESS_SCORE_ID"
        const val INTENSITY_FACTOR = "FIELD_INTENSITY_FACTOR_ID"

        /**
         * Optional
         */
        const val MAX_ELEVATION = "FIELD_MAX_ELEVATION_ID"

        /**
         * Optional
         */
        const val MIN_ELEVATION = "FIELD_MIN_ELEVATION_ID"

        /**
         * Optional
         */
        const val AVERAGE_ELEVATION = "FIELD_AVERAGE_ELEVATION_ID"

        /**
         * Optional
         */
        const val LAP_NUMBER = "FIELD_LAP_NUMBER_ID"

        /**
         * Optional
         */
        const val BATTERY_PERCENT = "FIELD_BATTERY_PERCENT_ID"

        /**
         * Alternate type: INT
         *   - 0: north
         *   - 1: northeast
         *   - 2: east
         *   - 3: southeast
         *   - 4: south
         *   - 5: southwest
         *   - 6: west
         *   - 7: northwest
         *
         * Preferable to use [LOC_BEARING] value from [Type.LOCATION]
         */
        const val HEADING = "FIELD_HEADING_ID"

        /**
         * Alternate type: INT - ordinal of [BatteryStatus]
         * Optional
         */
        const val SHIFTING_BATTERY_STATUS = "FIELD_SHIFTING_BATTERY_STATUS_ID"

        /**
         * Alternate type: INT - ordinal of [BatteryStatus]
         * Optional
         */
        const val SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR = "FIELD_SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR_ID"

        /**
         * Alternate type: INT - ordinal of [BatteryStatus]
         * Optional
         */
        const val SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR = "FIELD_SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val SHIFTING_FRONT_GEAR = "FIELD_SHIFTING_FRONT_GEAR_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val SHIFTING_FRONT_GEAR_TEETH = "FIELD_SHIFTING_FRONT_GEAR_TEETH_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val SHIFTING_FRONT_GEAR_MAX = "FIELD_SHIFTING_FRONT_GEAR_MAX_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val SHIFTING_REAR_GEAR = "FIELD_SHIFTING_REAR_GEAR_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val SHIFTING_REAR_GEAR_TEETH = "FIELD_SHIFTING_REAR_GEAR_TEETH_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         * Optional
         */
        const val SHIFTING_REAR_GEAR_MAX = "FIELD_SHIFTING_REAR_GEAR_MAX_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val SHIFTING_COUNT = "FIELD_SHIFTING_COUNT_ID"
        const val TIRE_PRESSURE = "FIELD_TIRE_PRESSURE_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         *   - 0: Unknown
         *   - 1: Front
         *   - 2: Rear
         */
        const val TIRE_PRESSURE_POSITION = "FIELD_TIRE_PRESSURE_POSITION_ID"

        /**
         * Optional
         */
        const val TIRE_PRESSURE_TARGET = "FIELD_TIRE_PRESSURE_TARGET_ID"

        /**
         * Optional
         */
        const val TIRE_PRESSURE_RANGE = "FIELD_TIRE_PRESSURE_RANGE_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val RADAR_THREAT_LEVEL = "FIELD_RADAR_THREAT_LEVEL_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_1_RANGE = "FIELD_RADAR_TARGET_1_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_2_RANGE = "FIELD_RADAR_TARGET_2_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_3_RANGE = "FIELD_RADAR_TARGET_3_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_4_RANGE = "FIELD_RADAR_TARGET_4_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_5_RANGE = "FIELD_RADAR_TARGET_5_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_6_RANGE = "FIELD_RADAR_TARGET_6_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_7_RANGE = "FIELD_RADAR_TARGET_7_RANGE_ID"

        /**
         * Optional
         */
        const val RADAR_TARGET_8_RANGE = "FIELD_RADAR_TARGET_8_RANGE_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val RADAR_ERROR = "FIELD_RADAR_ERROR_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         *
         * Percentage of LEV charge (out of 100)
         */
        const val LEV_BATTERY_STATUS = "FIELD_LEV_BATTERY_STATUS_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         *
         * Index of assist mode (out of [LEV_SUPPORTED_ASSIST_MODES]) which are manufacturer specific.
         */
        const val LEV_ASSIST_MODE = "FIELD_LEV_ASSIST_MODE_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val LEV_SUPPORTED_ASSIST_MODES = "FIELD_LEV_SUPPORTED_ASSIST_MODES_ID"
        const val LEV_ENERGY_CONSUMPTION = "FIELD_LEV_ENERGY_CONSUMPTION_ID"
        const val LEV_20MIN_ENERGY_CONSUMPTION = "FIELD_LEV_20MIN_ENERGY_CONSUMPTION_ID"

        /**
         * Alternate type: INT_POSITIVE_OR_ZERO
         */
        const val LEV_MOTOR_ASSIST_PERCENTAGE = "FIELD_LEV_MOTOR_ASSIST_PERCENTAGE_ID"
        const val LEV_MOTOR_POWER = "FIELD_LEV_MOTOR_POWER_ID"
        const val LEV_COMBINED_POWER = "FIELD_LEV_COMBINED_POWER_ID"
        const val LEV_ESTIMATED_RANGE = "FIELD_LEV_ESTIMATED_RANGE_ID"
    }

    companion object {

        /**
         * Construct full data type id from extension id and type id.
         */
        fun dataTypeId(extension: String, typeId: String): String {
            return "TYPE_EXT$SEPARATOR$extension$SEPARATOR$typeId"
        }

        /**
         * Parse full data type id string into extension/typeId parts.
         *
         * Also allows safe calling with extension::typeId
         *
         * @return Pair(extension, typeId)
         */
        fun fromDataType(dataTypeId: String): Pair<String, String>? {
            return if (dataTypeId.contains(SEPARATOR)) {
                val start = "TYPE_EXT$SEPARATOR"
                val parts = dataTypeId.removePrefix(start).split(SEPARATOR)
                if (parts.size == 2) {
                    return Pair(parts[0], parts[1])
                } else {
                    null
                }
            } else {
                null
            }
        }

        /**
         * @suppress
         */
        private const val SEPARATOR = "::"
    }
}
