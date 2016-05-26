Option Explicit
On Error Resume Next

Const en_US = 1033
Const adVarChar = 200
Const adModeRead = 1

SetLocale(en_US)

Dim conn : Set conn = CreateObject("ADODB.Connection")
conn.Mode = adModeRead
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
      PrintProperty(x)
    Next
  Else
    For Each x In properties
      Dim i : For i = 1 to Wscript.Arguments.Count - 1
        If StrComp(x.Name, Wscript.Arguments(i)) = 0 Then
          PrintProperty(x)
          Exit For
        End If
      Next
    Next
  End If
End Sub

Sub PrintProperty(x)
  Wscript.StdOut.WriteLine x.Name & vbTab & x.Value
End Sub

Sub CheckErr()
  If Err.Number <> 0 Then
    WScript.StdOut.WriteLine vbCrLf & Err.Number & vbTab & Err.Description
    Wscript.quit(1)
  End If
End Sub
