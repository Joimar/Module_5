#!/bin/bash

# Caminho para o APK do seu aplicativo (ajuste se necessário)
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
# Nome do pacote do seu aplicativo
PACKAGE_NAME="com.example.vehicleequalizernative"
# TAG para filtrar logs no Logcat
LOGCAT_TAG="VehicleEqualizerApp"

echo "Iniciando script de implantação e teste..."

# 1. Verificar se um dispositivo/emulador está conectado
if ! adb devices | grep -q "device"; then
  echo "Nenhum dispositivo ou emulador Android encontrado. Certifique-se de que um esteja conectado e online."
  exit 1
fi
echo "Dispositivo/emulador encontrado."

# 2. Desinstalar a versão anterior do aplicativo (se existir)
echo "Desinstalando versão anterior do $PACKAGE_NAME..."
adb uninstall $PACKAGE_NAME

# 3. Instalar o novo APK
echo "Instalando $APK_PATH..."
adb install $APK_PATH
if [ $? -ne 0 ]; then
  echo "Erro na instalação do APK. Verifique o caminho e as permissões."
  exit 1
fi
echo "Aplicativo instalado com sucesso."

# 4. Iniciar o aplicativo
echo "Iniciando o aplicativo $PACKAGE_NAME..."
adb shell am start -n "$PACKAGE_NAME/.MainActivity"

# 5. Esperar um pouco para o aplicativo iniciar
sleep 5

# 6. Capturar logs relevantes (simulando um teste de funcionalidade)
echo "Capturando logs do Logcat para $LOGCAT_TAG..."
adb logcat -d -s $LOGCAT_TAG > app_logs.txt
echo "Logs salvos em app_logs.txt"

# 7. Simular interações (ex: ligar o equalizador via shell)
# NOTA: Interações complexas de UI via shell são difíceis. Isso é apenas um exemplo.
# Para testes de UI mais robustos, use ferramentas como UI Automator ou Espresso.
echo "Simulando interação: Enviando broadcast para ativar equalizador (conceitual)..."
# Em um cenário real, você poderia ter um BroadcastReceiver no app para isso
adb shell am broadcast -a "com.example.vehicleequalizerapp.ACTION_SET_EQUALIZER_STATE" --ez "state" true
sleep 2
echo "Verificando logs após simulação..."
adb logcat -d -s $LOGCAT_TAG >> app_logs.txt
echo "Testes básicos concluídos. Verifique app_logs.txt para detalhes."

# 8. Forçar o fechamento do aplicativo (opcional)
# adb shell am force-stop $PACKAGE_NAME
echo "Script finalizado."
