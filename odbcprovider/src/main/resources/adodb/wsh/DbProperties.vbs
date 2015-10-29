Option Explicit
On Error Resume Next

Const en_US = 1033
Const adVarChar = 200

SetLocale(en_US)

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Open Wscript.Arguments.Item(0)
CheckErr()

PrintProperties(conn.Properties)
CheckErr()

conn.Close : Set conn = Nothing

Sub PrintProperties(properties)
  Wscript.StdOut.WriteLine "Name" & vbTab & "Value"
  Wscript.StdOut.WriteLine adVarChar & vbTab & adVarChar
  Dim x
  If Wscript.Arguments.Count = 1 Then
    For Each x In properties
      Wscript.StdOut.WriteLine x.Name & vbTab & x.Value
    Next
  Else
    Dim i : For i = 1 to Wscript.Arguments.Count - 1
      Set x = properties.Item(Wscript.Arguments(i))
      Wscript.StdOut.WriteLine x.Name & vbTab & x.Value
    Next
  End If
End Sub

Sub CheckErr()
  If Err.Number <> 0 Then
    WScript.StdOut.WriteLine Err.Number & ">>>" & Err.Description
    Wscript.quit(1)
  End If
End Sub
