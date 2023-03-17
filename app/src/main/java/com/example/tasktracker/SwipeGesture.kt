package com.example.tasktracker


import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import com.example.tasktracker.R as R1


abstract class SwipeGesture(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val completedColor = ContextCompat.getColor(context, R.color.material_deep_teal_500)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(completedColor)
            .addSwipeLeftActionIcon(R1.drawable.baseline_checklist_24)
            .setSwipeLeftLabelColor(R1.color.white)
            .addSwipeLeftLabel("Completed")
            .create()
            .decorate()
        viewHolder.itemView.alpha = 1 - Math.abs(dX) / viewHolder.itemView.width
        viewHolder.itemView.translationX = dX

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}


