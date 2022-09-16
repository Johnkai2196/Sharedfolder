package com.example.sharedfolder

import android.app.Activity
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedfolder.ui.theme.SharedFolderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    companion object {
        private lateinit var uri: Uri
        val model = FilesViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SharedFolderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column() {
                        Button(onClick = { openDocument() }) {
                            Text("open")
                        }
                        showList(model = model)
                    }
                }
            }
        }
    }

    private fun openDocument() {
        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT_TREE
        )
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.also { data ->
                uri = data.data ?: return@registerForActivityResult
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                val documentsTree =
                    DocumentFile.fromTreeUri(
                        application,
                        uri
                    )
                model.filesList.value = listOf("")
                if (documentsTree != null) {
                    listTree(documentsTree, model)
                }

            }
        }
    }
}

fun listTree(documentsTree: DocumentFile, model: FilesViewModel, intent: String = "") {
    if (documentsTree.isDirectory) {
        val childDocuments = documentsTree.listFiles()
        childDocuments.forEach { file ->
            Log.i("File", "${file.name} ${file.isDirectory}")
            file.name?.let { model.addFile(intent + it) }
            if (file.isDirectory) {
                listTree(file, model, "$intent\t>")
            }
        }
    }
}

class FilesViewModel : ViewModel() {
    val filesList = MutableLiveData<List<String>>(null)
    val files = MutableLiveData<String>()
    fun addFile(file: String) {
        filesList.value = filesList.value?.plus(file) ?: listOf(file)
    }

}

@Composable
fun showList(model: FilesViewModel) {
    val value: List<String>? by model.filesList.observeAsState(null)
    val lee: String by model.files.observeAsState("")
    Column {
        LazyColumn {
            value?.let {
                items(it) {
                    Text(text = it)
                }
            }
        }
    }
}


