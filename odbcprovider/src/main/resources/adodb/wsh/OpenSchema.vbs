Option Explicit
On Error Resume Next

Const en_US = 1033
Const adSchemaTables = 20

SetLocale(en_US)

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Open Wscript.Arguments.Item(0)
CheckErr()

Dim rs : Set rs = conn.OpenSchema(adSchemaTables, Array(GetArgOrEmpty(1), GetArgOrEmpty(2), GetArgOrEmpty(3), Empty))
CheckErr()

PrintHeaders(rs)
PrintData(rs)
CheckErr()

rs.Close : Set rs = Nothing
conn.Close : Set conn = Nothing

Sub PrintHeaders(rs)
  Dim out : Set out = Wscript.StdOut
  Dim fields : Set fields = rs.Fields
  Dim i

  For i = 0 to fields.Count - 2
    out.Write fields.Item(i).Name & vbTab
  Next
  out.WriteLine fields.Item(fields.Count - 1).Name

  For i = 0 to fields.Count - 2
    out.Write fields.Item(i).Type & vbTab
  Next
  out.WriteLine rs.Fields.Item(rs.Fields.Count - 1).Type
End Sub

Sub PrintData(rs)
  If Not (rs.EOF) Then
    Dim out : Set out = Wscript.StdOut
    Dim fields : Set fields = rs.Fields
    Dim i
    Do Until rs.EOF
      If IsValidType(fields) Then
        For i = 0 to fields.Count - 2
          out.Write fields.Item(i).Value & vbTab
        Next
        out.WriteLine fields.Item(fields.Count - 1).Value & ""
      End If
      rs.MoveNext
    Loop 
  End If
End Sub

Function IsValidType(fields)
  IsValidType = true
  If Wscript.Arguments.Count > 4 Then
    Dim i
    For i = 4 to Wscript.Arguments.Count - 1
      If fields("TABLE_TYPE") = Wscript.Arguments.Item(i) Then
        Exit Function
      End If
    Next
    IsValidType = false
  End If
End Function

Function GetArgOrEmpty(position)
  If Wscript.Arguments.Count > position Then
    If Len(Wscript.Arguments.Item(position)) > 0 Then
      GetArgOrEmpty = Wscript.Arguments.Item(position)
    Else
      GetArgOrEmpty = Empty
    End If
  Else
    GetArgOrEmpty = Empty
  End If
End Function

Sub CheckErr()
  If Err.Number <> 0 Then
    WScript.StdOut.WriteLine Err.Number & ">>>" & Err.Description
    Wscript.quit(1)
  End If
End Sub
