package me.alexeyterekhov.vkfilter.Test

import android.os.Bundle
import android.os.Handler
import android.util.Log
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogListActivity
import java.util.Vector

public class TestDialogActivityAnimations: DialogListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("TEST ANIMATIONS")
    }

    override fun onRefresh() {
        Log.d("debug", "TEST ANIMATIONS")
        Handler().postDelayed({
            val mutated = updateDialog(DialogListCache.getSnapshot(), 2)
            DialogListCache.updateSnapshot(mutated)
        }, 500)
    }

    fun updateDialog(snap: DialogListSnapshot, pos: Int): DialogListSnapshot {
        val d = snap.dialogs
        if (pos < 0 || pos >= d.size())
            throw IllegalArgumentException()

        val before = d.subList(0, pos)
        val removed = d.get(pos)
        val after = d.subList(pos + 1, d.size())

        val updated = Dialog()
        with (updated) {
            id = removed.id
            lastMessage = Message(removed.lastMessage!!.sender)
            photoUrl = removed.photoUrl
            title = removed.title
            addPartner(removed.lastMessage!!.sender)
        }
        with (updated.lastMessage!!) {
            dateMSC = System.currentTimeMillis()
            isRead = false
            formattedDate = DateFormat.dialogReceivedDate(dateMSC / 1000L)
            text = "Updated Test Text"
            isOut = false
        }

        val newDialogs = Vector<Dialog>()
        newDialogs add updated
        newDialogs addAll before
        newDialogs addAll after
        return DialogListSnapshot(System.currentTimeMillis(), newDialogs)
    }

    fun oneDialog(snap: DialogListSnapshot): DialogListSnapshot {
        val d = snap.dialogs
        return if (d.size() > 2) {
            val newDialogs = Vector<Dialog>()
            newDialogs add d[2]
            newDialogs addAll d
            newDialogs removeElementAt 3
            DialogListSnapshot(System.currentTimeMillis(), newDialogs)
        } else snap
    }

    fun twoDialogs(snap: DialogListSnapshot): DialogListSnapshot {
        val d = snap.dialogs
        return if (d.size() > 3) {
            val newDialogs = Vector<Dialog>()
            newDialogs add d[2]
            newDialogs add d[3]
            newDialogs addAll d
            newDialogs removeElementAt 4
            newDialogs removeElementAt 5
            DialogListSnapshot(System.currentTimeMillis(), newDialogs)
        } else snap
    }
}