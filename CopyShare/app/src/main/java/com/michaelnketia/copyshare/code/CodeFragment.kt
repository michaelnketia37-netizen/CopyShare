package com.michaelnketia.copyshare.code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaelnketia.copyshare.databinding.FragmentCodeBinding
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

class CodeFragment : Fragment() {

    private var _binding: FragmentCodeBinding? = null
    private val binding get() = _binding!!

    private lateinit var editor: CodeEditor
    private val fileList = mutableListOf<String>()
    private lateinit var adapter: CodeFileAdapter
    private lateinit var projectDir: File
    private var currentFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCodeBinding.inflate(inflater, container, false)

        editor = binding.codeEditor

        projectDir = File(requireContext().filesDir, "CodeProjects/MyProject")

        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        setupRecycler()
        setupButtons()
        loadFiles()

        return binding.root
    }

    private fun setupRecycler() {
        adapter = CodeFileAdapter(fileList) {
            openFile(it)
        }

        binding.recyclerFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFiles.adapter = adapter
    }

    private fun setupButtons() {

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.sidebarLayout)
        }

        binding.btnSave.setOnClickListener {
            saveCurrentFile()
        }

        binding.btnCopy.setOnClickListener {
            copyText()
        }

        binding.btnPaste.setOnClickListener {
            pasteText()
        }

        binding.btnNewFile.setOnClickListener {
            createFileDialog()
        }
    }

    private fun createFileDialog() {

        val input = EditText(requireContext())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New File")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->

                val name = input.text.toString()

                if (name.isNotEmpty()) {
                    val file = File(projectDir, name)

                    file.writeText("// New File")

                    loadFiles()

                    openFile(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadFiles() {

        fileList.clear()

        projectDir.listFiles()?.forEach {
            fileList.add(it.name)
        }

        adapter.notifyDataSetChanged()
    }

    private fun openFile(name: String) {

        val file = File(projectDir, name)

        currentFile = file

        binding.txtFileName.text = name

        editor.setText(file.readText())

        log("Opened $name")

        binding.drawerLayout.closeDrawer(binding.sidebarLayout)
    }

    private fun saveCurrentFile() {

        val file = currentFile ?: return

        file.writeText(editor.text.toString())

        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()

        log("Saved ${file.name}")
    }

    private fun copyText() {

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText("code", editor.text.toString())
        )

        Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun pasteText() {

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()

        editor.setText(text)
    }

    private fun log(message: String) {
        binding.logs.append("\n$message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}