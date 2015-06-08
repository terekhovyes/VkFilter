package me.alexeyterekhov.vkfilter.GUI.Mock

import com.rockerhieu.emojicon.emoji.People
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Helpers.ChatInfo
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot
import java.util.ArrayList
import java.util.HashMap
import java.util.Vector


public object Mocker {
    val MOCK_MODE = false

    fun mockDialogSnapshot(): DialogListSnapshot {
        val dialogs = generateDialogs()
        val chats = generateChats()
        val dialogList = dialogs plus chats sortDescendingBy { it.lastMessage!!.sentTimeMillis }
        return DialogListSnapshot(System.currentTimeMillis(), Vector(dialogList))
    }

    fun mockFilters(): List<VkFilter> {
        val out = ArrayList<VkFilter>()

        val allow = VkFilter.STATE_ALLOWING
        val idle = VkFilter.STATE_DISABLED
        val block = VkFilter.STATE_BLOCKING

        val filterNames = arrayListOf("Друзья", "Лиза", "Антон", "Чатик с коллегами")
        val icons = arrayListOf(1, 5, 3, 1)
        val states = arrayListOf(allow, allow, block, idle)
        val identifiers = arrayListOf(
                arrayListOf(
                        userId(1),
                        userId(2),
                        userId(3),
                        chatId(101)
                ),
                arrayListOf(userId(4)),
                arrayListOf(userId(2)),
                arrayListOf(userId(5))
        )

        val photos = hashMapOf(
                1 to "http://api.randomuser.me/portraits/men/48.jpg",
                2 to "http://api.randomuser.me/portraits/men/44.jpg",
                3 to "http://api.randomuser.me/portraits/women/27.jpg",
                4 to "http://api.randomuser.me/portraits/women/49.jpg",
                5 to "http://www.kulturologia.ru/files/u9749/GrainofSand3.jpg"
        )
        val chatUsers = hashMapOf(
                101 to arrayListOf(1, 3, 4)
        )
        cacheUsers(photos)
        cacheChatInfo(chatUsers)

        for (i in 0..filterNames.size() - 1) {
            val filter = VkFilter()
            with (filter) {
                filterName = filterNames[i]
                iconId = icons[i]
                state = states[i]
                cached = true
                cachedIds addAll identifiers[i]
            }
            out add filter
        }

        return out
    }

//    fun mockMessagePack(): MessagePack {
//        val pack = MessagePack()
//
//        val texts = arrayListOf(
//                "Ну что, готов завтра выступать? ${People.DATA[11].getEmoji()}",
//                "Быстро неделя пролетела, уже четверг",
//                "Хорошо, в конце недели спишемся ${People.DATA[3].getEmoji()}",
//                "Концерт в пятницу в 8",
//                "Ну.. ещё есть время потренироваться)",
//                "Как раз за ударкой сижу, уже более-менее выходит",
//                "Ты уже всю партию выучил? Мне только соляк в конце остался",
//                "Всё про выступление думаю"
//        )
//        val cur = System.currentTimeMillis()
//        val dates = arrayListOf(
//                cur - 1000 * 60 * 5,
//                cur - 1000 * 60 * 6,
//                cur - 1000 * 60 * 60 * 75,
//                cur - 1000 * 60 * 60 * 75 - 1000 * 60 * 60,
//                cur - 1000 * 60 * 60 * 75 - 1000 * 60 * 60,
//                cur - 1000 * 60 * 60 * 75 - 1000 * 60 * 120,
//                cur - 1000 * 60 * 60 * 75 - 1000 * 60 * 240,
//                cur - 1000 * 60 * 60 * 75 - 1000 * 60 * 600
//        )
//        val income = arrayListOf(
//                true,
//                true,
//                true,
//                false,
//                false,
//                true,
//                false,
//                false
//        )
//
//        val messages = ArrayList<MessageOld>()
//        for (i in texts.size() - 1 downTo 0) {
//            val m = MessageOld("")
//            with (m) {
//                id = i.toLong()
//                text = texts[i]
//                isOut = !income[i]
//                dateMSC = dates[i]
//                isRead = true
//            }
//            messages add m
//        }
//
//        pack.messages addAll messages
//
//        return pack
//    }

    private fun generateDialogs(): ArrayList<Dialog> {
        val out = ArrayList<Dialog>()

        val firstNames = arrayListOf("Алексей", "Юлия", "Елизавета")
        val lastNames = arrayListOf("Картофанов", "Василькова", "Караваева")
        val photos = arrayListOf(
                "http://api.randomuser.me/portraits/men/48.jpg",
                "http://api.randomuser.me/portraits/women/27.jpg",
                "http://api.randomuser.me/portraits/women/49.jpg"
        )
        val messages = arrayListOf(
                "Ну что, готов завтра выступать? ${People.DATA[11].getEmoji()}",
                "Только не говори, что не сегодня!",
                "Получилось?! ${People.DATA[54].getEmoji()}"
        )
        val income = arrayListOf(
                true,
                false,
                false
        )
        val online = arrayListOf(
                true,
                false,
                false
        )
        val read = arrayListOf(
                false,
                true,
                false
        )
        val imageAttachments = arrayListOf(
                null,
                ImageAttachment("", "", 0, 0),
                null
        )
        val cur = System.currentTimeMillis()
        val date = arrayListOf(
                cur - 1000 * 60 * 5,
                cur - 1000 * 60 * 7,
                cur - 1000 * 60 * 68
        )

        UserCache.putUser(generateMe())
        for (i in 0..firstNames.size() - 1) {
            val partner = User()
            with (partner) {
                id = "$i"
                firstName = firstNames[i]
                lastName = lastNames[i]
                photoUrl = photos[i]
                isOnline = online[i]
            }
            UserCache.putUser(partner)
//            val message = MessageOld( if (income[i]) partner.id else "me" )
//            with (message) {
//                text = messages[i]
//                isRead = read[i]
//                isOut = !income[i]
//                dateMSC = date[i]
//                if (imageAttachments[i] != null)
//                    attachments.images add imageAttachments[i]!!
//            }

//            val dialog = Dialog()
//            with (dialog) {
//                id = i.toLong()
//                addPartner(partner)
//                lastMessage = message.toNewFormat()
//            }
            //out add dialog
        }

        return out
    }

    private fun generateChats(): ArrayList<Dialog> {
        val out = ArrayList<Dialog>()

        val chatTitles = arrayListOf("Тусовка на выходных")
        val chatPhotos = arrayListOf(
                arrayListOf(
                        "http://api.randomuser.me/portraits/men/48.jpg",
                        "http://api.randomuser.me/portraits/women/27.jpg",
                        "http://api.randomuser.me/portraits/women/49.jpg"
                )
        )
        val messages = arrayListOf(
                "Приходите, буду очень рад :)"
        )
        val read = arrayListOf(
                true
        )
        val cur = System.currentTimeMillis()
        val date = arrayListOf(
                cur - 1000 * 60 * 283
        )

        UserCache.putUser(generateMe())
        for (i in 0..chatTitles.size() - 1) {
            val dialog = Dialog()

            dialog.title = chatTitles[i]
            for (j in 0..chatPhotos[i].size() - 1) {
                val partner = User()
                partner.id = j.toString()
                partner.photoUrl = chatPhotos[i][j]
                dialog.addPartner(partner)
            }
            val message = Message("me")
            with (message) {
                text = messages[i]
                isRead = read[i]
                isOut = true
                sentTimeMillis = date[i]
            }

            with (dialog) {
                id = i.toLong() + 100
                lastMessage = message
            }
            out add dialog
        }

        return out
    }

    private fun generateMe(): User {
        val me = User()
        with (me) {
            id = "me"
            photoUrl = "http://api.randomuser.me/portraits/men/13.jpg"
            firstName = "Сергей"
            lastName = "Деловитый"
        }
        return me
    }

    private fun userId(id: Long): VkIdentifier {
        val vk = VkIdentifier()
        vk.id = id
        vk.type = VkIdentifier.TYPE_USER
        return vk
    }

    private fun chatId(id: Long): VkIdentifier {
        val vk = VkIdentifier()
        vk.id = id
        vk.type = VkIdentifier.TYPE_CHAT
        return vk
    }

    private fun cacheUsers(idToPhoto: HashMap<Int, String>) {
        idToPhoto forEach {
            val id = it.getKey()
            val photo = it.getValue()
            val user = User()
            with (user) {
                user.id = id.toString()
                photoUrl = photo
            }
            UserCache.putUser(user)
        }
    }

    private fun cacheChatInfo(idToUsers: HashMap<Int, ArrayList<Int>>) {
        idToUsers forEach {
            val chatId = it.getKey()
            val userIds = it.getValue()
            val chatInfo = ChatInfo()
            with (chatInfo) {
                id = chatId.toLong()
                chatPartners addAll (userIds map { UserCache.getUser(it.toString()) })
            }
            ChatInfoCache.putChat(chatInfo.id.toString(), chatInfo)
        }
    }
}