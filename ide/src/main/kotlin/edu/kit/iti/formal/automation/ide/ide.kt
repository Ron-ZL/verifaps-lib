package edu.kit.iti.formal.automation.ide

import com.vlsolutions.swing.docking.Dockable
import com.vlsolutions.swing.docking.DockingConstants
import com.vlsolutions.swing.docking.DockingDesktop
import com.vlsolutions.swing.docking.RelativeDockablePosition
import me.tomassetti.kanvas.languageSupportRegistry
import me.tomassetti.kanvas.noneLanguageSupport
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.text.ParseException
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import kotlin.properties.Delegates


/**
 *
 * @author Alexander Weigl
 * @version 1 (10.03.19)
 */
class Ide(rootLookup: Lookup) : JFrame(),
        GetFileChooser, FileOpen,
        TabManagement, ActionService {

    private var fontSize: Float by Delegates.observable(12f) { prop, _, new ->
        if (fontSize < 6f) fontSize = 6f
        else {
            defaultFont = defaultFont.deriveFont(new)
            jMenuBar.font = jMenuBar.font.deriveFont(new)
        }
    }

    val lookup = Lookup(rootLookup)

    var defaultFont by Delegates.observable(Font(Font.MONOSPACED, Font.PLAIN, 12)) { _, _, new ->
        lookup.getAll<EditorPane>().forEach {
            it.textFont = new
        }
    }

    val editorFactories = arrayListOf<(File) -> EditorPane?>()

    val allEditors: List<EditorPane>
        get() {
            return lookup.getAll()
        }

    val currentEditor: EditorPane?
        get() = currentTabbedPanel as? EditorPane

    val currentTabbedPanel: TabbedPanel?
        get() {
            var cur = focusOwner
            while (cur != null && cur !is TabbedPanel) {
                cur = cur.parent
            }
            return cur as? TabbedPanel
        }

    override val fileChooser = JFileChooser()
    val defaultToolBar = JToolBar()

    val actions = arrayListOf<IdeAction>()

    val actionSaveAs = createAction(name = "Save As", menuPath = "File", prio = 3,
            fontIcon = FontAwesomeSolid.SAVE) { saveAs() }

    val actionRun = createAction(name = "Run", menuPath = "Tools", prio = 9,
            fontIcon = FontAwesomeSolid.RUNNING) { runCurrentProgram() }

    val actionSave = createAction("Save", "File",
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), 2,
            fontIcon = FontAwesomeRegular.SAVE) { save() }

    val actionNew = createAction("New", "File",
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), 0,
            fontIcon = FontAwesomeRegular.FILE) { createCodeEditor() }
    val actionOpen = createAction("Open", "File",
            KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), 1,
            fontIcon = FontAwesomeRegular.FOLDER_OPEN) { open() }
    val actionClose = createAction("Close", "File",
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), 4,
            fontIcon = FontAwesomeSolid.WINDOW_CLOSE) { close() }

    val actionIncrFontSize = createAction("Increase Font Size", "View",
            KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK),
            fontIcon = FontAwesomeSolid.PLUS) { ++fontSize }
    val actionDecrFontSize = createAction("Decrease Font Size", "View",
            KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK),
            fontIcon = FontAwesomeSolid.MINUS) { --fontSize }

    val recentFiles by lookup.with<RecentFilesService>()

    val actionClearRecentFiles = createAction("Clear recent files",
            "File.Recent Files", prio = 5) {
        recentFiles.clear()
        addRecentFiles()
    }


    private fun runCurrentProgram() {
        val editor = (null as? STEditor)
        if (editor != null) {
            try {
                val runnerWindow = RunnerWindow(lookup, editor)
                addToolTab(runnerWindow)
            } catch (e: ParseException) {

            }
        }
    }

    var globalPort = DockingDesktop()

    init {
        lookup.register<GetFileChooser>(this)
        lookup.register<FileOpen>(this)
        lookup.register<ProblemList>(ProblemList())


        editorFactories.add {
            if (it.name.endsWith("tt.txt")) TTEditor(lookup).also { e ->
                e.file = it
                e.textArea.text = it.readText()
            }
            else null
        }
        editorFactories.add {
            if (it.name.endsWith("st")
                    || it.name.endsWith("iec")
                    || it.name.endsWith("iec61131")) STEditor(lookup).also { e ->
                e.file = it
                e.textArea.text = it.readText()
            }
            else null
        }

        defaultToolBar.add(actionNew)
        defaultToolBar.add(actionOpen)
        defaultToolBar.add(actionSave)
        defaultToolBar.add(actionClose)

        title = "IEC61131 Mini Ide"
        iconImage = IconFontSwing.buildImage(FontAwesomeSolid.PENCIL_RULER, 16f)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.layout = BorderLayout(5, 5)
        contentPane.add(defaultToolBar, BorderLayout.NORTH)
        contentPane.add(globalPort, BorderLayout.CENTER)

        jMenuBar = JMenuBar()
        actions.sortedBy { it.priority }
                .forEach { jMenuBar import it }
        addRecentFiles()

        val tree = FileTreePanel(lookup)
        addEditorTab(STEditor(lookup))
        globalPort.addDockable(tree, RelativeDockablePosition.LEFT_CENTER)
        addToolTab(GetetaPreview(lookup))
        addToolTab(GetetaWindow(lookup))
        addToolTab(ProblemPanel(lookup))

        size = Dimension(700, 800)

        invalidate()
        revalidate()
        revalidate()
        repaint()
        repaint()
    }

    override fun registerAction(act: IdeAction) {
        actions += act
        addActionIntoMenubar(act)
        addActionIntoToolbar(act)
    }

    private fun addActionIntoToolbar(act: IdeAction) {
        if (act.toolbarId == null) {
            defaultToolBar.add(act)
        }
    }

    override fun deregisterAction(act: IdeAction) {
        actions += act
        removeActionFromMenubar(act)
        removeActionFromToolbar(act)
    }

    private fun removeActionFromToolbar(act: IdeAction) {
        if (act.toolbarId != null) {
            for (it in defaultToolBar.components) {
                if (it is JButton && it.action == act) {
                    defaultToolBar.remove(it)
                    break
                }
            }
        }
    }

    private fun removeActionFromMenubar(act: IdeAction) {
        if (act.menuPath.isNotEmpty()) {
            //TODO
            val a = jMenuBar.getOrCreate(act.menuPath)
        }
    }

    fun getDockable(pred: (Dockable?) -> Boolean): Dockable? {
        return when {
            pred(globalPort.selectedDockable) -> globalPort.selectedDockable
            else -> globalPort.dockables.findLast { pred(it.dockable) && it.isDocked }?.dockable
        }
    }

    override fun addEditorTab(editor: EditorPane) {
        val editorPane: Dockable? = getDockable { it is EditorPane }
        if (editorPane == null)
            globalPort.addDockable(editor, RelativeDockablePosition.TOP_CENTER)
        else
            globalPort.createTab(editorPane, editor, 0, true)
        lookup.register(editor)
    }

    fun createCodeEditor() = addEditorTab(STEditor(lookup))

    fun close(editor: Closeable? = currentTabbedPanel) {
        editor?.also {
            it.close()
        }
    }

    fun saveAs(editor: EditorPane? = currentEditor) =
            editor?.also { it.saveAs() }

    fun save(editor: EditorPane? = currentEditor) {
        editor?.also { it.save() }
    }

    fun open() {
        val fc = JFileChooser()
        val res = fc.showOpenDialog(this)
        if (res == JFileChooser.APPROVE_OPTION) {
            open(fc.selectedFile)
        }
    }

    override fun open(f: File) {
        if (f !in recentFiles) {
            recentFiles.add(f)
            addRecentFiles()
        }
        for (it in editorFactories) {
            val p = it(f)
            if (p != null) {
                addEditorTab(p)
                break
            }
        }
    }

    override fun addToolTab(window: ToolPane) {
        val otherToolWindow = getDockable { it is ToolPane && it != null }
        if (otherToolWindow != null)
            globalPort.createTab(otherToolWindow, window, 0, true)
        else {
            val editor = getDockable { it is EditorPane } ?: window
            globalPort.split(editor, window, DockingConstants.SPLIT_BOTTOM)// RelativeDockablePosition.LEFT_CENTER)
        }
        lookup.register(window)
    }

    fun createAndShowKanvasGUI() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        try {
            val xToolkit = Toolkit.getDefaultToolkit()
            println("XTOOLKIT " + xToolkit)
            val awtAppClassNameField = xToolkit.javaClass.getDeclaredField("awtAppClassName")
            awtAppClassNameField.isAccessible = true
            awtAppClassNameField.set(xToolkit, title)
        } catch (e: Exception) {
            // ignore
            System.err.println(e.message)
        }
        isVisible = true
    }

    fun addActionIntoMenubar(act: Action) = jMenuBar import act

    private fun addRecentFiles() {
        val m = "File.Recent Files"
        var i = 0
        jMenuBar.getOrCreate("File").getOrCreate("Recent Files").removeAll()
        recentFiles.recentFiles
                .map { rf ->
                    createAction(rf.absolutePath, m, prio = i++, fontIcon = FontAwesomeSolid.SAVE) {
                        open(rf)
                    }
                }
                .sortedBy { it.priority }
                .forEach { jMenuBar import it }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val rootLookup = Lookup()
            rootLookup.register<RecentFilesService>(RecentFilesImpl())
            rootLookup.register(Colors())

            //https://tomassetti.me/kanvas-generating-simple-ide-antlr-grammar/
            val iecSupport = IECLanguageSupport(rootLookup)
            val ttSupport = TestTableLanguageSupport(rootLookup)
            languageSupportRegistry.register("st", iecSupport)
            languageSupportRegistry.register("iec61131", iecSupport)
            languageSupportRegistry.register(".tt", ttSupport)
            languageSupportRegistry.register(".tt.txt", ttSupport)
            languageSupportRegistry.register(".testtable", ttSupport)

            val ide = Ide(rootLookup)
            SwingUtilities.invokeLater {
                ide.createAndShowKanvasGUI()
                args.forEach {
                    val f = File(it)
                    ide.open(f)
                }
            }
        }
    }
}

private infix fun JMenuBar.import(it: Action) {
    it.getValue(ACTION_MENU_PATH_KEY)?.also { path ->
        val iter = path.toString().splitToSequence('.').iterator()
        if (!iter.hasNext()) return
        var current = getOrCreate(iter.next())
        iter.forEachRemaining {
            current = this.getOrCreate(it)
        }
        current.add(it)
    }
}

private fun JComponent.getOrCreate(key: String): JMenu {
    this.components.forEach {
        if (it is JMenu && it.text == key) return it
    }
    val m = JMenu(key)
    this.add(m)
    return m
}

class FileTreePanel(lookup: Lookup) : TabbedPanel(BorderLayout()) {
    val lookup = Lookup(lookup)
    val contextMenu = JPopupMenu()
    val treeFiles = JTree(DefaultTreeModel(FolderTreeNode(File(".").absoluteFile, this::fileFilter)))

    val actionOpenFile = createAction("Open File", "") {
        val file = treeFiles.selectionModel.selectionPath.lastPathComponent as FolderTreeNode
        lookup.get<FileOpen>().open(file.file)
    }

    val actionGoUp = createAction("Go Up", "") {
        val file = treeFiles.model.root as? FolderTreeNode
        if (file != null) {
            val m = DefaultTreeModel(FolderTreeNode(file.file.parentFile.absoluteFile, this::fileFilter))
            treeFiles.model = m
        }
    }

    val actionGoInto = createAction("Go Into", "") {
        val file = treeFiles.selectionModel?.selectionPath?.lastPathComponent as? FolderTreeNode
        if (file != null) {
            val m = DefaultTreeModel(FolderTreeNode(file.file, this::fileFilter))
            treeFiles.model = m
        }
    }

    val actionOpenExplorer = createAction("Open in Explorer", "") {
    }

    val actionOpenSystem = createAction("Open in System", "") {
        val file = treeFiles.selectionModel?.selectionPath?.lastPathComponent as? FolderTreeNode
        if (file != null) {
            val m = DefaultTreeModel(FolderTreeNode(file.file, this::fileFilter))
            treeFiles.model = m
        }
    }


    val actionRefresh = createAction("Refresh", "") {}

    init {
        title = "Navigator"
        dockKey.isCloseEnabled = false

        treeFiles.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                println(e.isPopupTrigger)
                if (e.isPopupTrigger) {
                    contextMenu.show(e.component, e.x, e.y)
                    println("FileTreePanel.mousePressed")
                }
            }
        })

        contextMenu.add(actionGoUp)
        contextMenu.add(actionGoInto)
        contextMenu.add(actionOpenFile)
        contextMenu.add(actionRefresh)
        contextMenu.add(actionOpenExplorer)
        contextMenu.add(actionOpenSystem)

        treeFiles.cellRenderer = object : DefaultTreeCellRenderer() {
            override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
                val f = value as FolderTreeNode
                return super.getTreeCellRendererComponent(tree, f.file.name, sel, expanded, leaf, row, hasFocus)
            }
        }
        add(JScrollPane(treeFiles))
    }


    //Not closeable
    override fun close() {}


    private fun fileFilter(file: File): Boolean {
        if (file.isDirectory) return true
        return languageSupportRegistry.languageSupportForFile(file) != noneLanguageSupport
    }
}

data class FolderTreeNode(val file: File,
                          val filter: (File) -> Boolean) : TreeNode {
    var children: List<FolderTreeNode> = arrayListOf()
        get() {
            if (field.isEmpty())
                field = file.listFiles()
                        .filter(filter)
                        .map { FolderTreeNode(it, filter) }
            return field
        }

    override fun children(): Enumeration<out TreeNode> = Collections.enumeration(children)
    override fun isLeaf(): Boolean = !file.isDirectory
    override fun getChildCount(): Int = children.size
    override fun getParent(): TreeNode = FolderTreeNode(file.parentFile, filter)
    override fun getChildAt(childIndex: Int): TreeNode = children[childIndex]
    override fun getIndex(node: TreeNode?): Int = children.indexOf(node)
    override fun getAllowsChildren(): Boolean = isLeaf
}