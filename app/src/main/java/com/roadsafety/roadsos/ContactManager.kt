package com.roadsafety.roadsos

import android.content.Context

import org.json.JSONArray
import org.json.JSONObject

object ContactManager {

    private const val PREF_NAME =
        "contacts_pref"

    private const val KEY_CONTACTS =
        "contacts"

    fun saveContact(

        context: Context,

        contact: Contact
    ) {

        val contacts =
            getContacts(context)
                .toMutableList()

        contacts.add(contact)

        saveAllContacts(
            context,
            contacts
        )
    }

    fun getContacts(
        context: Context
    ): List<Contact> {

        val contacts =
            mutableListOf<Contact>()

        val sharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val jsonString =
            sharedPreferences.getString(
                KEY_CONTACTS,
                "[]"
            )

        val jsonArray =
            JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {

            val obj =
                jsonArray.getJSONObject(i)

            contacts.add(

                Contact(

                    obj.getString("id"),

                    obj.getString("name"),

                    obj.getString("phone"),

                    obj.getString("relation")
                )
            )
        }

        return contacts
    }

    fun removeContact(

        context: Context,

        contactId: String
    ) {

        val updatedList =
            getContacts(context)
                .filter {

                    it.id != contactId
                }

        saveAllContacts(
            context,
            updatedList
        )
    }

    private fun saveAllContacts(

        context: Context,

        contacts: List<Contact>
    ) {

        val jsonArray =
            JSONArray()

        for (contact in contacts) {

            val obj =
                JSONObject()

            obj.put(
                "id",
                contact.id
            )

            obj.put(
                "name",
                contact.name
            )

            obj.put(
                "phone",
                contact.phone
            )

            obj.put(
                "relation",
                contact.relation
            )

            jsonArray.put(obj)
        }

        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_CONTACTS,
                jsonArray.toString()
            )
            .apply()
    }
}