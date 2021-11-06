<?php
define("google_server_key", "AAAA3Knmk-I:APA91bHPwzEmm49ni76KxqrPNskbGq36KktDcAWAikljUT9iui8-_mf3zsLJXRhxl7HUbgt502O7RXMLtRc9eLglash4qvHrRTw2dn5fBtHh3FoeJAy_P1BVLwzUdNLoY0KcRlRHTgNH");
	function send_notification ($tokens, $message)
	{
		$url = 'https://fcm.googleapis.com/fcm/send';

    $fields = array(
			 'registration_ids' => $tokens,
			 'data' => $message,
			 'click_action' => 'LoginActivity'
			);
		$headers = array(
			'Authorization:key =' .google_server_key,
			'Content-Type: application/json'
			);

	   $ch = curl_init();
       curl_setopt($ch, CURLOPT_URL, $url);
       curl_setopt($ch, CURLOPT_POST, true);
       curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
       curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
       curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);
       curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
       curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
       $result = curl_exec($ch);
       if ($result === FALSE) {
           die('Curl failed: ' . curl_error($ch));
       }
       curl_close($ch);
       return $result;
	}
 ?>
