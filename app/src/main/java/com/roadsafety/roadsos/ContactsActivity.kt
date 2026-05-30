package com.roadsafety.roadsos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
<<<<<<< HEAD
import com.google.firebase.auth.FirebaseAuth
=======
import com.roadsafety.roadsos.ContactManager
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3

class ContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: ContactsAdapter
<<<<<<< HEAD
    private var contactList = mutableListOf<Contact>()

    // Firebase Connect karne ke liye
    private val firestoreManager = FirestoreManager()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
=======
    private var contactList =
        mutableListOf<Contact>()
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.contactsRecyclerView)
        emptyState = findViewById(R.id.emptyState)

<<<<<<< HEAD
=======
        // Setup RecyclerView
        contactList.addAll(
            ContactManager.getContacts(this)
        )
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        adapter = ContactsAdapter(contactList) { contact, position ->
            showDeleteDialog(contact, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Add contact button
        findViewById<MaterialButton>(R.id.addContactButton).setOnClickListener {
            showAddContactDialog()
        }

<<<<<<< HEAD
        // App khulte hi Firebase se Contacts fetch karna
        fetchContactsFromFirebase()
    }

    private fun fetchContactsFromFirebase() {
        if (userId == null) return

        Toast.makeText(this, "Loading contacts...", Toast.LENGTH_SHORT).show()

        firestoreManager.getEmergencyContacts(userId) { contacts, error ->
            if (contacts != null) {
                contactList.clear()
                contactList.addAll(contacts)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            } else {
                Toast.makeText(this, "Failed to load: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
=======
        updateEmptyState()
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_contact, null)
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3

        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("ADD") { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.inputName).text.toString().trim()
                val phone = dialogView.findViewById<EditText>(R.id.inputPhone).text.toString().trim()
                val relation = dialogView.findViewById<EditText>(R.id.inputRelation).text.toString().trim()

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

<<<<<<< HEAD
                // ID me timestamp use kar rahe hain taaki har contact unique ho
=======
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
                val contact = Contact(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    phone = phone,
                    relation = relation.ifEmpty { "Contact" }
                )
<<<<<<< HEAD

                // 1. UI me turant add karo taaki user ko fast feel ho
                adapter.addContact(contact)
                updateEmptyState()

                // 2. Background me Firebase par save karo
                if (userId != null) {
                    firestoreManager.addEmergencyContact(userId, contact) { success, error ->
                        if (success) {
                            Toast.makeText(this, "$name saved to Cloud", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Cloud Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
=======
                adapter.addContact(contact)
                ContactManager.saveContact(
                    this,
                    contact
                )
                updateEmptyState()
                Toast.makeText(this, "${name} added", Toast.LENGTH_SHORT).show()
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showDeleteDialog(contact: Contact, position: Int) {
        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Remove Contact")
            .setMessage("Remove ${contact.name} from emergency contacts?")
            .setPositiveButton("REMOVE") { _, _ ->
<<<<<<< HEAD

                // 1. UI se turant hatao
                adapter.removeContact(position)
                updateEmptyState()

                // 2. Firebase se bhi delete karo
                if (userId != null) {
                    firestoreManager.deleteEmergencyContact(userId, contact.id) { success, error ->
                        if (success) {
                            Toast.makeText(this, "Contact removed from Cloud", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Cloud Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
=======
                adapter.removeContact(position)
                ContactManager.removeContact(
                    this,
                    contact.id
                )
                updateEmptyState()
                Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show()
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun updateEmptyState() {
        if (contactList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}