package com.training.notesapp

import NotesAdapter
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.training.notesapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var noteDataBase: NoteDataBase
    lateinit var noteAdapter: NotesAdapter
    lateinit var noteList: MutableList<Note>

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noteDataBase = NoteDataBase(this)
        noteList = mutableListOf()
        noteAdapter = NotesAdapter(noteList)
        binding.notesRecyclerView.adapter = noteAdapter
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.addButton.setOnClickListener { addNote() }
        binding.deleteButton.setOnClickListener { deleteNote() }
        binding.viewButton.setOnClickListener { viewNotes() }
        binding.importButton.setOnClickListener { importNotes() }
        binding.exportButton.setOnClickListener { exportNotes() }
    }

    private fun exportNotes() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            writeNotesToExternalStorage()
        }
    }

    private fun writeNotesToExternalStorage() {
        val file = File(Environment.getExternalStorageDirectory(), "notes.txt")
        if (file.exists()) {
            try {
                val outputStream = file.outputStream()
                val db = noteDataBase.readableDatabase
                val projection = arrayOf(Constant.TITLE, Constant.CONTENT)
                val cursor = db.query(
                    Constant.TABLE_NAME,
                    projection,
                    null, null, null, null, null, null
                )
                with(cursor) {
                    while (moveToNext()) {
                        val title = getString(getColumnIndexOrThrow(Constant.TITLE))
                        val content = getString(getColumnIndexOrThrow(Constant.CONTENT))
                        val line = "$title:$content"
                        outputStream.write(line.toByteArray())
                    }
                }
                outputStream.close()
                Toast.makeText(this, "Notes exported", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "Error writing file", Toast.LENGTH_SHORT).show()
            }
        }
    }





        private fun importNotes() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                readNotesFromExternalStorage()
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            when (requestCode) {
                PERMISSION_REQUEST_CODE -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (permissions[0] == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                            readNotesFromExternalStorage()
                        } else if (permissions[0] == android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                            writeNotesToExternalStorage()
                        } else {

                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }

        private fun readNotesFromExternalStorage() {
            val file = File(Environment.getExternalStorageDirectory(), "notes.txt")
            if (file.exists()) {
                try {
                    val inputStream = FileInputStream(file)
                    val text = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()
                    val lines = text.split("\n")
                    val db = noteDataBase.writableDatabase
                    lines.forEach { line ->
                        val parts = line.split(":")
                        if (parts.size == 2) {
                            val values = ContentValues().apply {
                                put(Constant.TITLE, parts[0])
                                put(Constant.CONTENT, parts[1])
                            }
                            db.insert(Constant.TABLE_NAME, null, values)
                        }
                    }
                    Toast.makeText(this, "Notes imported", Toast.LENGTH_SHORT).show()
                    viewNotes()
                } catch (e: IOException) {
                    Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No file found", Toast.LENGTH_SHORT).show()
            }

        }

        private fun deleteNote() {
            val title = binding.titleEditText.text.toString()
            if (title.isNotEmpty()) {
                val db = noteDataBase.writableDatabase
                val selection = "${Constant.TITLE} = ?"
                val selectionArgs = arrayOf(title)
                db.delete(Constant.TABLE_NAME, selection, selectionArgs)
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                clearFields()
                viewNotes()
            } else {
                Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
            }


        }

        private fun addNote() {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val db = noteDataBase.writableDatabase
                val values = ContentValues().apply {
                    put(Constant.TITLE, title)
                    put(Constant.CONTENT, content)
                }
                db.insert(Constant.TABLE_NAME, null, values)
                Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
                clearFields()
                viewNotes()
            } else {
                Toast.makeText(this, "Please enter title and content", Toast.LENGTH_SHORT).show()
            }

        }

        @SuppressLint("NotifyDataSetChanged")
        private fun viewNotes() {
            noteList.clear()
            val db = noteDataBase.readableDatabase
            val projection = arrayOf(Constant.TITLE, Constant.CONTENT)
            val cursor = db.query(
                Constant.TABLE_NAME,
                projection,
                null, null, null, null, null, null
            )
            with(cursor) {
                while (moveToNext()) {
                    val title = getString(getColumnIndexOrThrow(Constant.TITLE))
                    val content = getString(getColumnIndexOrThrow(Constant.CONTENT))
                    noteList.add(Note(title, content))
                }
            }
            noteAdapter.notifyDataSetChanged()

        }

        private fun clearFields() {
            binding.titleEditText.text.clear()
            binding.contentEditText.text.clear()
        }
    }