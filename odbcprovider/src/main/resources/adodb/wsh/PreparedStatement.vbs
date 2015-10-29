Option Explicit
On Error Resume Next

Const en_US = 1033

SetLocale(en_US)

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Open Wscript.Arguments.Item(0)
CheckErr()

Dim cmd : Set cmd = CreateObject("ADODB.Command")
cmd.ActiveConnection = conn
cmd.CommandText = Wscript.Arguments.Item(1)
CheckErr()

Dim i

For i = 2 to Wscript.Arguments.Count - 1
  cmd.Parameters.Item(i - 2).Value = Wscript.Arguments(i)
Next
CheckErr()

Dim rs : Set rs = cmd.Execute
CheckErr()

Dim out : Set out = Wscript.StdOut

For i = 0 to rs.Fields.Count - 2
  out.Write rs.Fields.Item(i).Name & vbTab
Next
out.WriteLine rs.Fields.Item(rs.Fields.Count - 1).Name

For i = 0 to rs.Fields.Count - 2
  out.Write rs.Fields.Item(i).Type & vbTab
Next
out.WriteLine rs.Fields.Item(rs.Fields.Count - 1).Type

If Not (rs.EOF) Then
  Do Until rs.EOF
    For i = 0 to rs.Fields.Count - 2
      out.Write rs.Fields.Item(i).Value & vbTab
    Next
    out.WriteLine rs.Fields.Item(rs.Fields.Count - 1).Value & ""
    rs.MoveNext
  Loop 
End If
CheckErr()

rs.Close : Set rs = Nothing
conn.Close : Set conn = Nothing

Sub CheckErr()
  If Err.Number <> 0 Then
    WScript.StdOut.WriteLine Err.Number & vbTab & Err.Description
    Wscript.quit(1)
  End If
End Sub
