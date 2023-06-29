FROM ubuntu AS idea

WORKDIR /app

RUN apt-get update \
  && apt-get install -y unzip \
  && rm -rf /var/lib/apt/lists/*

# file idea-*.tar.gz, downloaded from jetbrains
COPY build/ideaIC-2023.1.3.tar.gz idea.tar.gz
RUN tar -xzf idea.tar.gz \
    && rm idea.tar.gz \
    && mv idea-* idea \
    && cd idea \
    && mv plugins plugins-old \
    && mkdir plugins \
    && cp -r plugins-old/java plugins-old/java-ide-customization plugins-old/keymap-* plugins \
    && rm -r plugins-old


FROM ubuntu

WORKDIR /app/idea

RUN apt-get update \
  && apt-get install -y libfreetype6 fontconfig \
  && rm -rf /var/lib/apt/lists/*

COPY --from=idea /app/idea .

COPY build/distributions/formatter-plugin plugins/formatter-plugin

COPY formatter /usr/bin/formatter

WORKDIR /data

ENTRYPOINT ["formatter"]
