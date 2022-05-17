package de.ma.lululemon.api.domain.monitor.product


class Product{

    lateinit var id: String

    lateinit var color: String

    lateinit var size: String

    lateinit var name: String

    lateinit var url: String

    var states: MutableSet<State> = mutableSetOf()

    fun addState(state: State) {
        this.states.add(state)
    }
}

