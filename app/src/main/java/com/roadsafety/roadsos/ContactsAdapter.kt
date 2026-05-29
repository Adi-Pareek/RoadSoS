package com.roadsafety.roadsos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onDelete: (Contact, Int) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: TextView = view.findViewById(R.id.contactAvatar)
        val name: TextView = view.findViewById(R.id.contactName)
        val phone: TextView = view.findViewById(R.id.contactPhone)
        val relation: TextView = view.findViewById(R.id.contactRelation)
        val delete: TextView = view.findViewById(R.id.deleteContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        // Set first letter as avatar
        holder.avatar.text = contact.name.firstOrNull()?.uppercase() ?: "?"
        holder.name.text = contact.name
        holder.phone.text = contact.phone
        holder.relation.text = contact.relation

        // Delete button
        holder.delete.setOnClickListener {
            onDelete(contact, position)
        }
    }

    override fun getItemCount() = contacts.size

    // Add new contact to list
    fun addContact(contact: Contact) {
        contacts.add(contact)
        notifyItemInserted(contacts.size - 1)
    }

    // Remove contact from list
    fun removeContact(position: Int) {
        contacts.removeAt(position)
        notifyItemRemoved(position)
    }
}