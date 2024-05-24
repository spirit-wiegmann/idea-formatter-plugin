FROM ubuntu:jammy-20230624

WORKDIR /app

RUN apt-get update \
  && apt-get install -y unzip wget libfreetype6 fontconfig \
  && rm -rf /var/lib/apt/lists/*

RUN wget --progress=bar:force https://download.jetbrains.com/idea/ideaIC-2023.1.3.tar.gz \
    && tar -xzf ideaIC-2023.1.3.tar.gz \
    && rm ideaIC-2023.1.3.tar.gz \
    && mv idea-* idea \
    && cd idea \
    && mv plugins plugins-old \
    && mkdir plugins \
    && cp -r plugins-old/java  \
        plugins-old/java-ide-customization  \
        plugins-old/keymap-*  \
        plugins-old/properties \
        plugins-old/Groovy \
        plugins-old/yaml \
        plugins \
    && rm -r plugins-old

# PythonCore 231.8770.65
RUN wget --progress=bar:force -O python-ce.zip "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=326457" \
    && unzip python-ce.zip \
    && rm python-ce.zip \
    && mv python-ce idea/plugins/python-ce

WORKDIR /app/idea

COPY build/distributions/formatter-plugin plugins/formatter-plugin

COPY formatter /usr/bin/formatter

WORKDIR /data

ENTRYPOINT ["formatter"]
