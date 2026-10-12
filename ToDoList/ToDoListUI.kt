// Sample UI for the to-do list

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.awt.geom.Path2D
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

// TrailMate theme colors
private val BACKGROUND = Color(0x1B3A20)
private val GREEN = Color(0x109B55)
private val GREEN_PRESSED = Color(0x0C7E45)
private val TEXT = Color.WHITE
private val MUTED_TEXT = Color(0xB7C7B9)
private val ACCENT = Color(0xF2A93B)
private val LIST_BACKGROUND = Color(0x2C4F32)

private const val FONT_NAME = "Segoe UI"

private fun colorFor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> ACCENT
    Priority.LOW -> MUTED_TEXT
}

private fun smooth(g: Graphics): Graphics2D {
    val g2 = g.create() as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    return g2
}

// Green circle with two mountains and a sun
private class Logo : JComponent() {
    init {
        val size = Dimension(72, 72)
        preferredSize = size
        maximumSize = size
    }

    override fun paintComponent(g: Graphics) {
        val g2 = smooth(g)
        val w = width.toDouble()
        val h = height.toDouble()

        g2.color = GREEN
        g2.fillOval(0, 0, width, height)

        g2.color = ACCENT
        g2.fillOval((w * 0.60).toInt(), (h * 0.22).toInt(), (w * 0.14).toInt(), (h * 0.14).toInt())

        g2.color = BACKGROUND
        g2.fill(triangle(w * 0.18, h * 0.70, w * 0.42, h * 0.32, w * 0.66, h * 0.70))
        g2.fill(triangle(w * 0.46, h * 0.70, w * 0.64, h * 0.46, w * 0.82, h * 0.70))
        g2.dispose()
    }

    private fun triangle(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Path2D {
        val path = Path2D.Double()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.closePath()
        return path
    }
}

// White rounded box that holds the task field
private class RoundedField(private val field: JTextField) : JPanel(BorderLayout()) {
    init {
        isOpaque = false
        border = BorderFactory.createEmptyBorder(0, 14, 0, 14)
        field.border = null
        field.isOpaque = false
        field.font = Font(FONT_NAME, Font.PLAIN, 15)
        add(field, BorderLayout.CENTER)
        preferredSize = Dimension(0, 44)
        maximumSize = Dimension(Int.MAX_VALUE, 44)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = smooth(g)
        g2.color = Color.WHITE
        g2.fillRoundRect(0, 0, width, height, 16, 16)
        g2.dispose()
    }
}

private class RoundedButton(text: String) : JButton(text) {
    init {
        isContentAreaFilled = false
        isFocusPainted = false
        isBorderPainted = false
        foreground = TEXT
        font = Font(FONT_NAME, Font.BOLD, 15)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        preferredSize = Dimension(0, 48)
        maximumSize = Dimension(Int.MAX_VALUE, 48)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = smooth(g)
        g2.color = if (model.isPressed) GREEN_PRESSED else GREEN
        g2.fillRoundRect(0, 0, width, height, 16, 16)
        g2.dispose()
        super.paintComponent(g)
    }
}

// One row in the task list: priority tag on the left, task text beside it
private class TaskRow : ListCellRenderer<Task> {
    private val row = JPanel(BorderLayout(10, 0))
    private val tag = JLabel()
    private val text = JLabel()

    init {
        row.border = BorderFactory.createEmptyBorder(10, 12, 10, 12)
        tag.font = Font(FONT_NAME, Font.BOLD, 11)
        tag.preferredSize = Dimension(34, 18)
        text.font = Font(FONT_NAME, Font.PLAIN, 14)
        text.foreground = TEXT
        row.add(tag, BorderLayout.WEST)
        row.add(text, BorderLayout.CENTER)
    }

    override fun getListCellRendererComponent(
        list: JList<out Task>, task: Task, index: Int, isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        tag.text = task.priority.label.uppercase()
        tag.foreground = if (isSelected) TEXT else colorFor(task.priority)
        text.text = task.text
        row.background = if (isSelected) GREEN else LIST_BACKGROUND
        return row
    }
}

private fun label(text: String, style: Int, size: Int, color: Color): JLabel {
    // Stretch to the full row width so longer text is never clipped
    val label = object : JLabel(text) {
        override fun getMaximumSize() = Dimension(Int.MAX_VALUE, preferredSize.height)
    }
    label.font = Font(FONT_NAME, style, size)
    label.foreground = color
    return label
}

private fun priorityButton(priority: Priority): JRadioButton {
    val button = JRadioButton("${priority.label} priority")
    button.isOpaque = false
    button.isFocusPainted = false
    button.font = Font(FONT_NAME, Font.PLAIN, 13)
    button.foreground = MUTED_TEXT
    return button
}

fun createToDoListFrame(toDoList: ToDoList = ToDoList()): JFrame {
    val taskField = JTextField()
    val statusLabel = label(" ", Font.PLAIN, 13, ACCENT)

    val highButton = priorityButton(Priority.HIGH)
    val lowButton = priorityButton(Priority.LOW)
    val priorityGroup = ButtonGroup()
    priorityGroup.add(highButton)
    priorityGroup.add(lowButton)
    lowButton.isSelected = true

    val priorityRow = JPanel()
    priorityRow.layout = BoxLayout(priorityRow, BoxLayout.X_AXIS)
    priorityRow.isOpaque = false
    priorityRow.add(highButton)
    priorityRow.add(Box.createHorizontalStrut(16))
    priorityRow.add(lowButton)
    priorityRow.add(Box.createHorizontalGlue())

    val taskList = JList<Task>()
    taskList.cellRenderer = TaskRow()
    taskList.selectionMode = ListSelectionModel.SINGLE_SELECTION
    taskList.background = LIST_BACKGROUND

    val scroll = JScrollPane(taskList)
    scroll.border = null
    scroll.viewport.background = LIST_BACKGROUND
    scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

    fun chosenPriority(): Priority = if (highButton.isSelected) Priority.HIGH else Priority.LOW

    // Redraws the list and empties the inputs, ready for the next task
    fun refresh() {
        taskList.setListData(toDoList.all().toTypedArray())
        taskField.text = ""
        lowButton.isSelected = true
    }

    fun addTask() {
        if (toDoList.add(taskField.text, chosenPriority())) {
            refresh()
            statusLabel.text = "Task added."
        } else {
            statusLabel.text = "Type a task first."
        }
    }

    fun editTask() {
        val index = taskList.selectedIndex
        if (index < 0) {
            statusLabel.text = "Select a task to edit."
        } else if (toDoList.edit(index, taskField.text, chosenPriority())) {
            refresh()
            statusLabel.text = "Task updated."
        } else {
            statusLabel.text = "The task can't be blank."
        }
    }

    fun removeTask() {
        if (toDoList.remove(taskList.selectedIndex)) {
            refresh()
            statusLabel.text = "Task removed."
        } else {
            statusLabel.text = "Select a task to remove."
        }
    }

    // Selecting a task loads it into the inputs so it can be edited
    taskList.addListSelectionListener { event ->
        val task = taskList.selectedValue
        if (!event.valueIsAdjusting && task != null) {
            taskField.text = task.text
            if (task.priority == Priority.HIGH) highButton.isSelected = true else lowButton.isSelected = true
            statusLabel.text = "Change the task, then press Edit."
        }
    }

    val addButton = RoundedButton("Add")
    val editButton = RoundedButton("Edit")
    val removeButton = RoundedButton("Remove")
    addButton.addActionListener { addTask() }
    editButton.addActionListener { editTask() }
    removeButton.addActionListener { removeTask() }
    // Enter saves the selected task if there is one, otherwise adds a new task
    taskField.addActionListener { if (taskList.selectedIndex < 0) addTask() else editTask() }

    val buttons = JPanel(GridLayout(1, 3, 8, 0))
    buttons.isOpaque = false
    buttons.preferredSize = Dimension(0, 48)
    buttons.maximumSize = Dimension(Int.MAX_VALUE, 48)
    for (button in listOf(addButton, editButton, removeButton)) {
        buttons.add(button)
    }

    // Centered logo, app name, and tagline
    val header = JPanel()
    header.layout = BoxLayout(header, BoxLayout.Y_AXIS)
    header.isOpaque = false
    val logo = Logo()
    val appName = label("TrailMate", Font.BOLD, 26, TEXT)
    val tagline = label("Hike. Explore. Collect.", Font.PLAIN, 13, MUTED_TEXT)
    for (item in listOf<JComponent>(logo, appName, tagline)) {
        item.alignmentX = Component.CENTER_ALIGNMENT
    }
    appName.horizontalAlignment = JLabel.CENTER
    tagline.horizontalAlignment = JLabel.CENTER
    header.add(logo)
    header.add(Box.createVerticalStrut(10))
    header.add(appName)
    header.add(Box.createVerticalStrut(4))
    header.add(tagline)

    val content = JPanel()
    content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
    content.background = BACKGROUND
    content.border = BorderFactory.createEmptyBorder(32, 28, 28, 28)

    val rows = listOf(
        header to 24,
        label("To-Do List", Font.BOLD, 20, TEXT) to 4,
        label("Add tasks and rank them by priority", Font.PLAIN, 13, MUTED_TEXT) to 20,
        label("Task", Font.BOLD, 13, TEXT) to 8,
        RoundedField(taskField) to 6,
        priorityRow to 16,
        buttons to 8,
        statusLabel to 12,
        scroll to 0
    )
    for ((row, gapBelow) in rows) {
        row.alignmentX = Component.LEFT_ALIGNMENT
        content.add(row)
        content.add(Box.createVerticalStrut(gapBelow))
    }

    refresh()

    val frame = JFrame("TrailMate To-Do List")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane = content
    frame.size = Dimension(400, 720)
    frame.minimumSize = Dimension(340, 620)
    frame.setLocationRelativeTo(null)
    return frame
}

fun main() {
    SwingUtilities.invokeLater {
        createToDoListFrame().isVisible = true
    }
}
