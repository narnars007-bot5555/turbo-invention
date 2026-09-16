package com.tokar.frez.cnc.domain.usecase

import com.tokar.frez.cnc.data.entity.MachineEntity

data class DetailedMachineInfo(
    val machine: MachineEntity,
    val setupSteps: List<String>,
    val gcodeHandbook: Map<String, String>
)

class GetMachineDetailsUseCase {

    fun parseMachineDetails(machine: MachineEntity): DetailedMachineInfo {
        val steps = try {
            val list = mutableListOf<String>()
            val array = org.json.JSONArray(machine.setupGuideStepsJson)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            listOf(machine.setupGuideStepsJson)
        }

        val handbook = try {
            val map = mutableMapOf<String, String>()
            val obj = org.json.JSONObject(machine.gcodeHandbookJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
            map
        } catch (e: Exception) {
            mapOf("Info" to machine.gcodeHandbookJson)
        }

        return DetailedMachineInfo(
            machine = machine,
            setupSteps = steps,
            gcodeHandbook = handbook
        )
    }
}
