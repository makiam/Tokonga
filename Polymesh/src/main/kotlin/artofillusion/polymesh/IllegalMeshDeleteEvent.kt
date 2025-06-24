package artofillusion.polymesh

data class IllegalMeshDeleteEvent(val mesh: PolyMesh) {
    fun fire() {
        org.greenrobot.eventbus.EventBus.getDefault().post(this)
    }
}
