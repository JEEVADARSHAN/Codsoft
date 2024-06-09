package com.example.todo_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo_app.databinding.ViewTaskLayoutBinding
import com.example.todo_app.databinding.ViewTaskListBinding
import com.example.todo_app.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskRVVBListAdapter(
    private val isList: MutableLiveData<Boolean>,
    private val deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit,
) :
    ListAdapter<Task,RecyclerView.ViewHolder>(DiffCallback()) {



    class ListTaskViewHolder(private val viewTaskListLayoutBinding: ViewTaskListBinding) :
        RecyclerView.ViewHolder(viewTaskListLayoutBinding.root) {

        fun bind(
            task: Task,
            deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit,
        ) {
            viewTaskListLayoutBinding.titleTxt.text = task.title
            viewTaskListLayoutBinding.descrTxt.text = task.description

            val dateFormat =SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault())

            viewTaskListLayoutBinding.createDate.text ="Created: " + dateFormat.format(task.date)
            viewTaskListLayoutBinding.deleteImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("delete", adapterPosition, task)
                }
            }
            viewTaskListLayoutBinding.deadlineDate.text="Deadline: ${dateFormat.format(task.deadline)}"
            viewTaskListLayoutBinding.editImg.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("update", adapterPosition, task)
                }
            }
            viewTaskListLayoutBinding.status.text = task.status
        }
    }


    class GridTaskViewHolder(private val viewTaskGridLayoutBinding: ViewTaskLayoutBinding) :
        RecyclerView.ViewHolder(viewTaskGridLayoutBinding.root) {

        fun bind(
            task: Task,
            deleteUpdateCallback: (type: String, position: Int, task: Task) -> Unit,
        ) {
            viewTaskGridLayoutBinding.titleTxt.text = task.title
            viewTaskGridLayoutBinding.descrptionTxt.text = task.description

            val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault())

            viewTaskGridLayoutBinding.createDate.text = "Created: " +  dateFormat.format(task.date)
            viewTaskGridLayoutBinding.delete.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("delete", adapterPosition, task)
                }
            }
            viewTaskGridLayoutBinding.deadlineDate.text="Deadline: ${dateFormat.format(task.deadline)}"
            viewTaskGridLayoutBinding.editing.setOnClickListener {
                if (adapterPosition != -1) {
                    deleteUpdateCallback("update", adapterPosition, task)
                }
            }
            viewTaskGridLayoutBinding.status.text = task.status
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return if (viewType == 1){  // Grid_Item
            GridTaskViewHolder(
                ViewTaskLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }else{  // List_Item
            ListTaskViewHolder(
                ViewTaskListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = getItem(position)

        if (isList.value!!){
            (holder as ListTaskViewHolder).bind(task,deleteUpdateCallback)
        }else{
            (holder as GridTaskViewHolder).bind(task,deleteUpdateCallback)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (isList.value!!){
            0 // List_Item
        }else{
            1 // Grid_Item
        }
    }



    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

    }

}