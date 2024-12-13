import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter to manage and display a list of chatrooms in a RecyclerView
class ChatroomAdapter(
    private val groupNames: List<String>,
    private val groupIds: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ChatroomAdapter.ChatroomViewHolder>() {

    // ViewHolder binds and holds the UI components for each chatroom
    class ChatroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupNameTextView: TextView = view.findViewById(android.R.id.text1)
    }

    // Called when RecyclerView needs a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        // Inflate layout for the list item
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ChatroomViewHolder(view)
    }

    // Binds data ViewHolder, setting group name/click behavior
    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        // Group name text for position
        holder.groupNameTextView.text = groupNames[position]

        // Handle item click by triggering provided callback position
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    // Returns total number of items in dataset
    override fun getItemCount(): Int = groupNames.size
}
