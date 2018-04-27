package scala.meta.lsp

import scala.meta.jsonrpc.pickle._
import scala.meta.jsonrpc.json
import ujson.Js

/**
 * Position in a text document expressed as zero-based line and character offset.
 */
@json case class Position(line: Int, character: Int)

/**
 * A range in a text document.
 */
@json case class Range(start: Position, end: Position)

/**
 * Represents a location inside a resource, such as a line
 * inside a text file.
 */
@json case class Location(uri: String, range: Range)

/**
 * Represents a diagnostic, such as a compiler error or warning. Diagnostic objects are only valid
 * in the scope of a resource.
 *
 * @param range the range at which this diagnostic applies
 * @param severity severity of this diagnostics (see above)
 * @param code a code for this diagnostic
 * @param source the source of this diagnostic (like 'typescript' or 'scala')
 * @param message the diagnostic message
 */
@json case class Diagnostic(
    range: Range,
    severity: Option[DiagnosticSeverity] = None,
    code: Option[String] = None,
    source: Option[String] = None,
    message: String
)

/**
 * A reference to a command.
 *
 * @param title The title of the command, like 'Save'
 * @param command The identifier of the actual command handler
 * @param arguments The arugments this command may be invoked with
 */
@json case class Command(
    title: String,
    command: String,
    arguments: Seq[Js]
)

@json case class TextEdit(range: Range, newText: String)

/**
 * A workspace edit represents changes to many resources managed
 * in the workspace.
 */
@json case class WorkspaceEdit(
    changes: Map[String, Seq[TextEdit]] // uri -> changes
)

@json case class TextDocumentIdentifier(uri: String)

@json case class VersionedTextDocumentIdentifier(
    uri: String,
    version: Long
)

/**
 * An item to transfer a text document from the client to the
 * server.
 */
@json case class TextDocumentItem(
    uri: String,
    languageId: String,
    /**
     * The version number of this document (it will strictly increase after each
     * change, including undo/redo).
     */
    version: Long,
    text: String
)
@json case class CompletionItem(
    label: String,
    kind: Option[CompletionItemKind] = None,
    detail: Option[String] = None,
    documentation: Option[String] = None,
    sortText: Option[String] = None,
    filterText: Option[String] = None,
    insertText: Option[String] = None,
    textEdit: Option[String] = None,
    /** An data entry field that is preserved on a completion item between
     * a [CompletionRequest](#CompletionRequest) and a [CompletionResolveRequest]
     *   (#CompletionResolveRequest)
     */
    data: Option[String] = None
)

sealed trait MarkedString
object MarkedString {
  implicit val rw: ReadWriter[MarkedString] =
    readwriter[Js].bimap[MarkedString](
      {
        case m: RawMarkedString => write(m)
        case m: MarkdownString => write(m)
      }, { js =>
        if (js.obj.contains("value")) read[RawMarkedString](js)
        else read[MarkdownString](js)
      }
    )
}
@json case class RawMarkedString(language: String, value: String)
    extends MarkedString

@json case class MarkdownString(contents: String) extends MarkedString

@json case class ParameterInformation(
    label: String,
    documentation: Option[String] = None
)

@json case class SignatureInformation(
    label: String,
    documentation: Option[String] = None,
    parameters: Seq[ParameterInformation]
)

/**
 * Value-object that contains additional information when
 * requesting references.
 */
@json case class ReferenceContext(
    /** Include the declaration of the current symbol. */
    includeDeclaration: Boolean
)

/**
 * A document highlight is a range inside a text document which deserves
 * special attention. Usually a document highlight is visualized by changing
 * the background color of its range.
 */
@json case class DocumentHighlight(
    /** The range this highlight applies to. */
    range: Range,
    /** The highlight kind, default is [text](#DocumentHighlightKind.Text). */
    kind: DocumentHighlightKind = DocumentHighlightKind.Text
)

@json case class SymbolInformation(
    name: String,
    kind: SymbolKind,
    location: Location,
    containerName: Option[String] = None
)

/**
 * The parameters of a [WorkspaceSymbolRequest](#WorkspaceSymbolRequest).
 */
@json case class WorkspaceSymbolParams(query: String)

@json case class CodeActionContext(diagnostics: Seq[Diagnostic])

/**
 * A code lens represents a [command](#Command) that should be shown along with
 * source text, like the number of references, a way to run tests, etc.
 *
 * A code lens is _unresolved_ when no command is associated to it. For performance
 * reasons the creation of a code lens and resolving should be done to two stages.
 */
case class CodeLens(
    /**
     * The range in which this code lens is valid. Should only span a single line.
     */
    range: Range,
    /**
     * The command this code lens represents.
     */
    command: Option[Command] = None,
    /**
     * An data entry field that is preserved on a code lens item between
     * a [CodeLensRequest](#CodeLensRequest) and a [CodeLensResolveRequest]
     * (#CodeLensResolveRequest)
     */
    data: Option[Any] = None
)

/**
 * Value-object describing what options formatting should use.
 */
@json case class FormattingOptions(
    /**
     * Size of a tab in spaces.
     */
    tabSize: Int,
    /**
     * Prefer spaces over tabs.
     */
    insertSpaces: Boolean
    /**
     * Signature for further properties.
     */
    // params: Map[String, Any] // [key: string]: boolean | number | string;
)

/**
 * An event describing a change to a text document. If range and rangeLength are omitted
 * the new text is considered to be the full content of the document.
 */
@json case class TextDocumentContentChangeEvent(
    /**
     * The range of the document that changed.
     */
    range: Option[Range] = None,
    /**
     * The length of the range that got replaced.
     */
    rangeLength: Option[Int] = None,
    /**
     * The new text of the document.
     */
    text: String
)

@json case class DocumentFormattingParams(
    /**
     * The document to format.
     */
    textDocument: TextDocumentIdentifier,
    /**
     * The format options.
     */
    options: FormattingOptions
)

@json case class ExecuteCommandParams(
    command: String,
    arguments: Option[Seq[Js]] = None
)

/**
 * An event describing a file change.
 *
 * @param uri The file's URI
 * @param `type` The change type
 */
@json case class FileEvent(
    uri: String,
    `type`: FileChangeType
)
