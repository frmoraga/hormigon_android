<?php
/**
 * Handler para action=sendDailyReport
 * Agregar este case al switch de getData/index.phtml:
 *
 *   case 'sendDailyReport':
 *       require_once 'sendDailyReport.php';
 *       break;
 *
 * Campos POST recibidos:
 *   action          => "sendDailyReport"
 *   run_muestreador => RUN del muestreador
 *   id_muestreador  => ID interno del muestreador
 *   fecha           => Fecha del reporte (dd-MM-yyyy)
 *   cuerpo_html     => Cuerpo HTML pre-formateado por la app Android
 *   total           => Total de muestras planificadas
 *   tomadas         => Cantidad tomadas
 *   pendientes      => Cantidad pendientes
 *   suspendidas     => Cantidad suspendidas
 *   viaje_perdido   => Cantidad viaje perdido
 */

require_once '/home/www/html/intranet/dictuc/hormigon/webservice/getData/_lib/PHPMailerAutoload.php';

// ─── Lectura de parámetros ───────────────────────────────────────────────────

$runMuestreador = isset($_POST['run_muestreador']) ? intval($_POST['run_muestreador']) : -1;
$idMuestreador  = isset($_POST['id_muestreador'])  ? intval($_POST['id_muestreador'])  : -1;
$fecha          = isset($_POST['fecha'])            ? htmlspecialchars($_POST['fecha']) : date('d-m-Y');
$cuerpoHtml     = isset($_POST['cuerpo_html'])      ? $_POST['cuerpo_html']             : '';
$total          = isset($_POST['total'])            ? intval($_POST['total'])            : 0;
$tomadas        = isset($_POST['tomadas'])          ? intval($_POST['tomadas'])          : 0;
$pendientes     = isset($_POST['pendientes'])       ? intval($_POST['pendientes'])       : 0;
$suspendidas    = isset($_POST['suspendidas'])      ? intval($_POST['suspendidas'])      : 0;
$viajePerdido   = isset($_POST['viaje_perdido'])    ? intval($_POST['viaje_perdido'])    : 0;

// ─── Asunto ──────────────────────────────────────────────────────────────────

$asunto = "[DICTUC Android] Reporte diario $fecha — Tomadas: $tomadas/$total";

// ─── Envío via PHPMailer ──────────────────────────────────────────────────────

/** @var PHPMailer $mail */
$mail = new PHPMailer();
$mail->IsSMTP();
$mail->SMTPSecure = 'tls';
$mail->Host       = 'smtp.office365.com';
$mail->SMTPAuth   = true;
$mail->Port       = 25;
$mail->Username   = 'boleta_hormigon@dictuc.cl';
$mail->Password   = 'vcV+Ut<(E9^J_NHS';

$mail->SetFrom('boleta_hormigon@dictuc.cl');
$mail->Subject = $asunto;
$mail->MsgHTML($cuerpoHtml);
$mail->AddAddress('fmoraga@dictuc.cl');

$enviado = $mail->Send();

// ─── Log en servidor ─────────────────────────────────────────────────────────

$logLine = "[{$fecha}] id={$idMuestreador} run={$runMuestreador} total={$total} tomadas={$tomadas} pendientes={$pendientes} suspendidas={$suspendidas} viaje_perdido={$viajePerdido}\n";
file_put_contents(__DIR__ . '/daily_report.log', $logLine, FILE_APPEND | LOCK_EX);

echo $enviado ? '1' : '0';
